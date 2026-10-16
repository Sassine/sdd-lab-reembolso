import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Portão determinístico dos artefatos de SDD. Não depende de IA: lê os
 * arquivos em specs/ e reprova o que estiver fora do contrato.
 *
 * Uso (Java 21, sem compilar):
 *   java scripts/SddCheck.java                 verifica todas as specs
 *   java scripts/SddCheck.java specs/002-x     verifica uma spec
 *   java scripts/SddCheck.java --proxima specs/002-x   mostra a próxima tarefa pendente
 *
 * Saída 0 = tudo certo. Saída 1 = há erros (listados).
 */
public class SddCheck {

    static final Pattern REQUISITO_NA_SPEC = Pattern.compile("\\*\\*(R\\d+)\\*\\*");
    static final Pattern TAREFA = Pattern.compile("^- \\[( |x|X)\\] (T\\d{3})\\b(.*)$");
    static final Pattern MARCADOR = Pattern.compile("\\[([^\\]]+)\\]");
    static final Pattern FASE = Pattern.compile("^##\\s+(.+)$");
    static final String DUVIDA_ABERTA = "[PRECISA ESCLARECER";

    record Tarefa(String id, boolean feita, boolean paralela, Set<String> requisitos, String fase,
                  List<String> arquivos, String verificacao, String titulo, int linha) {}

    public static void main(String[] args) throws IOException {
        boolean proxima = args.length > 0 && args[0].equals("--proxima");
        List<Path> alvos = new ArrayList<>();
        for (String a : args) {
            if (!a.startsWith("--")) alvos.add(Path.of(a));
        }
        if (alvos.isEmpty()) {
            Path specs = Path.of("specs");
            if (!Files.isDirectory(specs)) {
                System.out.println("Nenhuma pasta specs/ encontrada. Rode a partir da raiz do repositório.");
                System.exit(1);
            }
            try (Stream<Path> s = Files.list(specs)) {
                s.filter(Files::isDirectory).sorted().forEach(alvos::add);
            }
        }

        List<String> erros = new ArrayList<>();
        for (Path dir : alvos) {
            verificar(dir, erros, proxima);
        }

        if (proxima) return;
        System.out.println();
        if (erros.isEmpty()) {
            System.out.println("SDD-CHECK OK");
        } else {
            System.out.println("SDD-CHECK FALHOU (" + erros.size() + "):");
            erros.forEach(e -> System.out.println("  - " + e));
            System.exit(1);
        }
    }

    static void verificar(Path dir, List<String> erros, boolean soProxima) throws IOException {
        String nome = dir.getFileName().toString();
        Path spec = dir.resolve("spec.md");
        Path bugfix = dir.resolve("bugfix.md");
        Path tasks = dir.resolve("tasks.md");
        Path progress = dir.resolve("progress.md");

        Path fonte = Files.exists(spec) ? spec : (Files.exists(bugfix) ? bugfix : null);
        if (fonte == null) {
            erros.add(nome + ": falta spec.md (ou bugfix.md)");
            return;
        }
        String textoSpec = Files.readString(fonte, StandardCharsets.UTF_8);
        Set<String> requisitos = new LinkedHashSet<>();
        Matcher m = REQUISITO_NA_SPEC.matcher(textoSpec);
        while (m.find()) requisitos.add(m.group(1));

        if (!Files.exists(tasks)) {
            if (!soProxima) {
                System.out.printf("%-34s %s, %d requisitos, sem tasks.md ainda%n",
                        nome, fonte.getFileName(), requisitos.size());
            }
            return;
        }

        if (textoSpec.contains(DUVIDA_ABERTA)) {
            erros.add(nome + ": a spec ainda tem [PRECISA ESCLARECER] e já existe tasks.md. Feche as dúvidas antes de quebrar em tarefas.");
        }

        List<Tarefa> tarefas = lerTarefas(tasks, erros, nome);

        if (soProxima) {
            tarefas.stream().filter(t -> !t.feita()).findFirst().ifPresentOrElse(
                    t -> {
                        System.out.println(t.id() + " " + t.titulo());
                        System.out.println("Fase: " + t.fase());
                        System.out.println("Requisitos: " + String.join(", ", t.requisitos()));
                        System.out.println("Arquivos: " + String.join(", ", t.arquivos()));
                        System.out.println("Verificação: " + t.verificacao());
                    },
                    () -> System.out.println("NENHUMA"));
            return;
        }

        Set<String> ids = new HashSet<>();
        Map<String, Map<String, String>> donoPorFase = new LinkedHashMap<>();
        for (Tarefa t : tarefas) {
            String ref = nome + "/tasks.md:" + t.linha() + " " + t.id();
            if (!ids.add(t.id())) erros.add(ref + ": ID repetido");
            if (t.requisitos().isEmpty()) {
                erros.add(ref + ": sem requisito. Use [R1], [R1,R3] ou [INFRA].");
            }
            for (String r : t.requisitos()) {
                if (!r.equals("INFRA") && !requisitos.contains(r)) {
                    erros.add(ref + ": cita " + r + ", que não existe em " + fonte.getFileName());
                }
            }
            if (t.arquivos().isEmpty()) erros.add(ref + ": falta a linha 'Arquivos:'");
            if (t.verificacao() == null || t.verificacao().isBlank()) {
                erros.add(ref + ": falta a linha 'Verificação:' (como provar que terminou)");
            }
            if (t.paralela()) {
                Map<String, String> donos = donoPorFase.computeIfAbsent(t.fase(), k -> new LinkedHashMap<>());
                for (String arq : t.arquivos()) {
                    String outro = donos.putIfAbsent(arq, t.id());
                    if (outro != null) {
                        erros.add(ref + ": é [P] mas divide " + arq + " com " + outro + " na mesma fase");
                    }
                }
            }
        }

        long feitas = tarefas.stream().filter(Tarefa::feita).count();
        if (feitas > 0) {
            if (!Files.exists(progress)) {
                erros.add(nome + ": há tarefas concluídas e não existe progress.md");
            } else {
                String log = Files.readString(progress, StandardCharsets.UTF_8);
                for (Tarefa t : tarefas) {
                    if (t.feita() && !log.contains(t.id())) {
                        erros.add(nome + ": " + t.id() + " está marcada como feita, mas não aparece no progress.md");
                    }
                }
            }
        }

        String proximaId = tarefas.stream().filter(t -> !t.feita()).map(Tarefa::id).findFirst().orElse("nenhuma");
        System.out.printf("%-34s %d/%d tarefas feitas, %d requisitos, próxima: %s%n",
                nome, feitas, tarefas.size(), requisitos.size(), proximaId);
    }

    static List<Tarefa> lerTarefas(Path tasks, List<String> erros, String nome) throws IOException {
        List<String> linhas = Files.readAllLines(tasks, StandardCharsets.UTF_8);
        List<Tarefa> tarefas = new ArrayList<>();
        String fase = "(sem fase)";
        for (int i = 0; i < linhas.size(); i++) {
            String linha = linhas.get(i);
            Matcher f = FASE.matcher(linha);
            if (f.matches()) {
                fase = f.group(1).trim();
                continue;
            }
            Matcher t = TAREFA.matcher(linha.trim());
            if (!t.matches()) continue;

            boolean feita = !t.group(1).isBlank();
            String id = t.group(2);
            String resto = t.group(3);
            boolean paralela = false;
            Set<String> reqs = new LinkedHashSet<>();
            Matcher mk = MARCADOR.matcher(resto);
            int fimMarcadores = 0;
            while (mk.find()) {
                String conteudo = mk.group(1).trim();
                if (conteudo.equals("P")) {
                    paralela = true;
                } else if (conteudo.matches("(R\\d+|INFRA)(\\s*,\\s*(R\\d+|INFRA))*")) {
                    for (String r : conteudo.split(",")) reqs.add(r.trim());
                } else {
                    continue;
                }
                fimMarcadores = mk.end();
            }
            String titulo = resto.substring(fimMarcadores).trim();

            List<String> arquivos = new ArrayList<>();
            String verificacao = null;
            for (int j = i + 1; j < linhas.size(); j++) {
                String seguinte = linhas.get(j).trim();
                if (TAREFA.matcher(seguinte).matches() || FASE.matcher(linhas.get(j)).matches()) break;
                if (seguinte.startsWith("Arquivos:")) {
                    for (String a : seguinte.substring("Arquivos:".length()).split(",")) {
                        String limpo = a.trim().replace("`", "");
                        if (!limpo.isEmpty()) arquivos.add(limpo);
                    }
                } else if (seguinte.startsWith("Verificação:")) {
                    verificacao = seguinte.substring("Verificação:".length()).trim();
                }
            }
            tarefas.add(new Tarefa(id, feita, paralela, reqs, fase, arquivos, verificacao, titulo, i + 1));
        }
        if (tarefas.isEmpty()) {
            erros.add(nome + "/tasks.md: nenhuma tarefa no formato '- [ ] T001 [R1] descrição'");
        }
        return tarefas;
    }
}
