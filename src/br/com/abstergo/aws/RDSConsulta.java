package br.com.abstergo.aws;

public class RDSConsulta {
    public static void main(String[] args) {
        String produto = "Dipirona 500mg";
        double precoAnterior = 12.90;
        double precoAtual = 9.75;

        System.out.println("Consultando banco RDS...");
        System.out.printf("Produto: %s%n", produto);
        System.out.printf("Preço anterior: R$ %.2f%n", precoAnterior);
        System.out.printf("Preço atual: R$ %.2f%n", precoAtual);
        System.out.println("Economia identificada: R$ " + (precoAnterior - precoAtual));
    }
}