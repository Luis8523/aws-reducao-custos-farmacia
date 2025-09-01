package br.com.abstergo.aws;

public class LambdaNotificacao {
    public static void main(String[] args) {
        String destinatario = "gerente@farmacia.com";
        String mensagem = "Relatório de redução de custos disponível no S3.";

        System.out.println("Executando função Lambda...");
        System.out.printf("Notificação enviada para %s: %s%n", destinatario, mensagem);
        System.out.println("Função Lambda finalizada.");
    }
}