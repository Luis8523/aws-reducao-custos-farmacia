package br.com.abstergo.aws;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class RelatorioFarmacia {

    public record Produto(String nome, double custoAntes, double custoDepois) {}

    public static void main(String[] args) {
        System.out.println("=== RELATÓRIO DE REDUÇÃO DE CUSTOS ===");

        List<Produto> produtosMesAnterior = List.of(
                new Produto("Dipirona", 5.20, 4.50),
                new Produto("Amoxicilina", 7.70, 6.90),
                new Produto("Ibuprofeno", 6.50, 5.80),
                new Produto("Paracetamol", 4.90, 4.20),
                new Produto("Omeprazol", 8.30, 7.10),
                new Produto("Loratadina", 9.50, 8.00),
                new Produto("Cetoconazol", 12.40, 10.90),
                new Produto("Metformina", 11.30, 9.80),
                new Produto("Losartana", 13.20, 11.50),
                new Produto("Sinvastatina", 14.10, 12.30)
        );
        double economiaAnterior = calcularEconomia(produtosMesAnterior);

        executarS3();
        executarLambda();
        executarRDS(produtosMesAnterior);

        LocalDateTime agora = LocalDateTime.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        System.out.println("Relatório gerado em: " + agora.format(formato));
        System.out.println();

        Scanner scanner = new Scanner(System.in);
        List<Produto> produtosMesAtual = new ArrayList<>();

        System.out.print("Deseja cadastrar novos produtos? (sim/não): ");
        String resposta = scanner.nextLine().trim().toLowerCase();

        if (resposta.equals("sim")) {
            System.out.print("Quantos produtos deseja cadastrar para este mês? ");
            int quantidade = scanner.nextInt();
            scanner.nextLine();

            for (int i = 0; i < quantidade; i++) {
                System.out.println("\nProduto #" + (i + 1));
                System.out.print("Nome: ");
                String nome = scanner.nextLine().trim();

                if (produtoJaExiste(produtosMesAtual, nome)) {
                    System.out.println("⚠️ Produto já cadastrado! Pulei este cadastro.");
                    continue;
                }

                System.out.print("Custo antes da migração: R$ ");
                double custoAntes = scanner.nextDouble();

                System.out.print("Custo depois da migração: R$ ");
                double custoDepois = scanner.nextDouble();
                scanner.nextLine();

                produtosMesAtual.add(new Produto(nome, custoAntes, custoDepois));
                System.out.println("✅ Produto cadastrado com sucesso.");
            }
        } else {
            System.out.println("Nenhum produto foi cadastrado para este mês.");
        }

        scanner.close();

        double economiaAtual = calcularEconomia(produtosMesAtual);

        if (!produtosMesAtual.isEmpty()) {
            System.out.println("\n--- RESUMO DOS PRODUTOS CADASTRADOS ---");
            System.out.printf("%-20s %-15s %-15s %-15s%n", "Produto", "Preço Antes", "Preço Depois", "Economia");

            for (Produto p : produtosMesAtual) {
                double economia = p.custoAntes() - p.custoDepois();
                System.out.printf("%-20s R$ %-12.2f R$ %-12.2f R$ %-12.2f%n",
                        p.nome(), p.custoAntes(), p.custoDepois(), economia);
            }

            System.out.println();
            System.out.printf("💰 Economia total este mês: R$ %.2f%n", economiaAtual);
        }

        double variacao = economiaAtual - economiaAnterior;
        double percentual = (economiaAnterior == 0) ? 0 : (variacao / economiaAnterior) * 100;

        System.out.println("\n📊 COMPARATIVO COM MÊS ANTERIOR:");
        System.out.printf("Economia mês anterior: R$ %.2f%n", economiaAnterior);
        System.out.printf("Economia este mês: R$ %.2f%n", economiaAtual);

        if (variacao > 0) {
            System.out.printf("✅ A economia aumentou em %.2f%% em relação ao mês anterior.%n", percentual);
        } else if (variacao < 0) {
            System.out.printf("⚠️ A economia caiu %.2f%% em relação ao mês anterior.%n", Math.abs(percentual));
        } else {
            System.out.println("🔄 A economia se manteve igual ao mês anterior.");
        }

        compararProdutos(produtosMesAnterior, produtosMesAtual);


        System.out.println("\nRelatório finalizado com sucesso.");
    }

    private static void compararProdutos(List<Produto> anterior, List<Produto> atual) {
        System.out.println("\n📌 COMPARATIVO POR PRODUTO:");
        System.out.printf("%-20s %-15s %-15s %-15s%n", "Produto", "Economia Anterior", "Economia Atual", "Variação");

        Map<String, Produto> mapaAnterior = new HashMap<>();
        for (Produto p : anterior) {
            mapaAnterior.put(p.nome().toLowerCase(), p);
        }

        for (Produto atualProd : atual) {
            String nome = atualProd.nome().toLowerCase();
            if (mapaAnterior.containsKey(nome)) {
                Produto anteriorProd = mapaAnterior.get(nome);
                double economiaAntes = anteriorProd.custoAntes() - anteriorProd.custoDepois();
                double economiaAgora = atualProd.custoAntes() - atualProd.custoDepois();
                double variacao = economiaAgora - economiaAntes;

                System.out.printf("%-20s R$ %-12.2f R$ %-12.2f R$ %-12.2f%n",
                        atualProd.nome(), economiaAntes, economiaAgora, variacao);
            }
        }
    }

    private static boolean produtoJaExiste(List<Produto> lista, String nome) {
        for (Produto p : lista) {
            if (p.nome().equalsIgnoreCase(nome)) {
                return true;
            }
        }
        return false;
    }

    private static void executarS3() {
        System.out.println("\n🔹 Etapa 1: Amazon S3");
        S3Upload.main(null);
    }

    private static void executarLambda() {
        System.out.println("\n🔹 Etapa 2: AWS Lambda");
        LambdaNotificacao.main(null);
    }

    private static void executarRDS(List<Produto> produtos) {
        System.out.println("\n🔹 Etapa 3: Amazon RDS");
        System.out.println("Consultando banco RDS...");

        System.out.printf("%-20s %-15s %-15s %-15s%n", "Produto", "Preço Anterior", "Preço Atual", "Economia");
        for (Produto p : produtos) {
            double economia = p.custoAntes() - p.custoDepois();
            System.out.printf("%-20s R$ %-12.2f R$ %-12.2f R$ %-12.2f%n",
                    p.nome(), p.custoAntes(), p.custoDepois(), economia);
        }

        double total = calcularEconomia(produtos);
        System.out.printf("💾 Economia identificada via RDS: R$ %.2f%n", total);
    }

    private static double calcularEconomia(List<Produto> produtos) {
        double total = 0;
        for (Produto p : produtos) {
            total += p.custoAntes() - p.custoDepois();
        }
        return total;
    }
}