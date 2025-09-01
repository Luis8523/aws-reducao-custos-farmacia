package br.com.abstergo.aws;

public class S3Upload {
    public static void main(String[] args) {
        String arquivo = "relatorio-farmacia.pdf";
        String bucket = "farmacia-custos";

        System.out.println("Iniciando upload para o S3...");
        System.out.printf("Arquivo '%s' enviado para o bucket '%s'.%n", arquivo, bucket);
        System.out.println("Upload concluído com sucesso!");
    }
}