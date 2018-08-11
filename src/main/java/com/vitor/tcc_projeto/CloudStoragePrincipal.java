package com.vitor.tcc_projeto;

import com.vitor.tcc_projeto.utils.FileHeader;
import java.awt.HeadlessException;
import org.jgroups.*;
import org.jgroups.conf.ClassConfigurator;
import org.jgroups.util.Util;

import java.util.*;
import java.io.*;
import java.net.InetAddress;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.hive2hive.core.api.H2HNode;
import org.hive2hive.core.api.configs.FileConfiguration;
import org.hive2hive.core.api.configs.NetworkConfiguration;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.api.interfaces.INetworkConfiguration;

/**
 *
 * @author Vitor
 */
public class CloudStoragePrincipal extends ReceiverAdapter {


    private JChannel channel;
    private final Map<String, OutputStream> cloudFiles = new ConcurrentHashMap<>();
    private File cloudFolder;
    private static final short ID = 3500;
    private static final Logger l = Logger.getLogger(CloudStoragePrincipal.class.getName());

    /**
     * Loop Principal, é aqui que serão escolhidos os arquivos que serão enviados para a nuvem
     *
     * @throws Exception Erro de arquivos
     */
    private void eventLoop() throws Exception {
        int saida;
        saida = 0;

        do {
            String texto = JOptionPane.showInputDialog(null, "Digite 1 para enviar um arquivo para a nuvem, ou 0 para sair");
            try {
                saida=Integer.parseInt(texto);
                String filepath;
                switch (saida) {
                    case 1:
                        JFileChooser chooser = new JFileChooser();
                        chooser.setCurrentDirectory(new java.io.File("."));
                        chooser.setDialogTitle("Escolha o Diretório");
                        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                        chooser.setAcceptAllFileFilterUsed(false);
                        int i = chooser.showOpenDialog(null);
                        if (i!=1){
                            filepath = chooser.getSelectedFile().toString();
                            sendFile(filepath);//função que envia o arquivo
                        }
                        break;
                    case 0:
                        l.log(Level.INFO,"saindo!!!");
                        break;
                    default:
                       l.log(Level.INFO,"Digite um numero(0,1)!");
                        break;
                }
            } catch (NumberFormatException e) {
                l.log(Level.SEVERE,"Houve algum erro!");
            }

        } while (saida != 0);
    }

    /**
     *
     * @param filepath
     * @throws Exception
     */
    protected void sendFile(String filepath) throws Exception {
        File inFile = new File(filepath);
        if( !inFile.exists() ){
            l.log(Level.SEVERE,"ERRO: não existe um arquivo com o caminho informado.");
            return;
        }
        FileInputStream in = new FileInputStream(inFile);

        //instanciando o meu FileInputStream com o meu arquivo ele criara a stream
        //para lermos o arquivo
        try {
            for (;;) {
                byte[] buf = new byte[4096];
                int bytesLidos = in.read(buf);
                if (bytesLidos == -1) {
                    break;
                }
                //vamos ler o aqruivo com o in.read e colocar os pedaços em pedacinhos menores de 8K
                sendMessage(buf, 0, bytesLidos, false, inFile);
                //enviando o pedaço criado, passo o buffer o inicio e o deslocamento e a flag indica o nao final do
                //arquivo
            }
        } catch (IOException e) {
            l.log(Level.SEVERE,e.getMessage());
        } finally {
            sendMessage(null, 0, 0, true, inFile);
            in.close();
            l.log(Level.INFO,inFile.getName());
            //mando vazio dizendo que o arquivo acabou
        }
    }

    /**
     *
     * @param msg
     */
    @Override
    public void receive(Message msg) {
        byte[] buf = msg.getRawBuffer();
        FileHeader hdr = (FileHeader) msg.getHeader(ID);//pegando o cabeçalho
        
        if (hdr == null) {
            return;
            //caso o cabeçalho nao exista eu não consigo fazer nada
        }
        
        OutputStream out = cloudFiles.get(hdr.getFileName());
        //criando a stream de saida                                        
        try {
            if (out == null) {//se a stream ta vazia tenho que instaciar nomes de arquivos, etc...
                File outFile = new File(this.cloudFolder + File.separator + hdr.getFileName());//pegando o nome que vem no cabeçalho
                boolean b = outFile.createNewFile();
                
                if(b){
                    out = new FileOutputStream(outFile);//instanciando a nova stream de saida com o nome do arquivo
                    cloudFiles.put(hdr.getFileName(), out);//adiciona no map o cabeçalho e a stream de saida
                }
            }
            if (hdr.isEof()) {
                Util.close(cloudFiles.remove(hdr.getFileName()));
                JOptionPane.showMessageDialog(null, "Novo arquivo obtido da nuvem: " + hdr.getFileName());
                //se acabar de receber o arquivo entao removo do meu map
            } else {
                if(out!=null){
                    out.write(msg.getRawBuffer(), msg.getOffset(), msg.getLength());
                }
                //caso contrario adiciono na minha stream de saida
            }
        } catch (HeadlessException | IOException t) {
            l.log(Level.SEVERE,t.getMessage());//caso houver falha
        }
    }

    /**
     *
     * @param buf
     * @param offset
     * @param length
     * @param eof
     * @param inFile
     */
    protected void sendMessage(byte[] buf, int offset, int length, boolean eof, File inFile) {
        Message msg = new Message(null, buf, offset, length).putHeader(ID, new FileHeader(inFile.getName(), eof));
        //instanciando nova mensagem com o null no campo de endereço iremos fazer
        //o multicast, passamos o buffer o deslocamento
        //e o colocamos um cabeçalho para verificar o fim do arquivo
        try {
            channel.send(msg);
        } catch (Exception e) {
            l.log(Level.SEVERE, e.getMessage());
        }
        //enviando a mensagem para o cluster
    }

    /**
     *
     * @throws Exception
     */
    private void start() throws Exception {

        IFileConfiguration fileConfiguration;

        String userHomeDir = System.getProperty("user.home") + File.separator + "Desktop";
        this.cloudFolder = new File(userHomeDir + File.separator + "VitorCloud");
        this.cloudFolder.mkdirs();
        
        ClassConfigurator.add((short) 3500, FileHeader.class);
        channel = new JChannel("sequencer.xml");//setando o nome do canal
        channel.setReceiver(this);//setando o receiver
        
        fileConfiguration = FileConfiguration.createDefault();
        IH2HNode peer = H2HNode.createNode(fileConfiguration);
        
        INetworkConfiguration netConfig = NetworkConfiguration.create("n-node",InetAddress.getLocalHost());
        peer.connect(netConfig);

        l.log(Level.INFO, "Conectado: {0}", peer.isConnected());
        channel.connect("Cluster de Arquivos");
        eventLoop();//iniciando o eventLoop que será responsável por mandar o arquivo
        channel.close();
    }

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception{
        new CloudStoragePrincipal().start();
    }
}
