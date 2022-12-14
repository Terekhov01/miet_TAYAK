import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PDA {

    static class Configuration {
        public char s; // состояние
        public String input; // оставшаяся часть входной ленты
        public String stack; // состояние магазина на данный момент
        public Configuration(char s, String p, String h) {
            this.s = s;
            this.input = p;
            this.stack = h;
        }
    }

    static class FLeft {
        public char s; // состояние
        public char p; // символ со входной ленты
        public char h; // магазинный символ

        public FLeft(char s, char p, char h){
            this.s = s;
            this.p = p;
            this.h = h;
        }
    }

    static class FRight {
        public char s;		// состояние
        public String c;	// заносимая цепочка

        public FRight(char s, String c){
            this.s = s;
            this.c = c;
        }
    }

    static class Command {
        public FLeft fargs;
        public ArrayList<FRight> values;

        public Command(FLeft f, ArrayList<FRight> v){
            this.fargs = f;
            this.values = v;
        }
    }

    static class ComBuilder {
        Set<Character> terminals = new HashSet<>();
        Set<Character> nonTerminals = new HashSet<>();
        char s0 = '0', h0 = '|', emptySym = '\0';
        List<Command> commands = new ArrayList<>();
        List<Configuration> chain = new ArrayList<>(); // цепочка конфигураций магазинного автомата, полученная в процессе его работы

        public ComBuilder(String filename) throws IOException {
            BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
            String tmpStr = br.readLine();
            int size;
            String exp = "([A-Z])>(.+)";
            Pattern pattern = Pattern.compile(exp);
            while (tmpStr != null && !tmpStr.equals("")) {
                Matcher matcher = pattern.matcher(tmpStr);
                if (!matcher.matches() ||
                        tmpStr.charAt(tmpStr.length() - 1) == '|' ||
                        tmpStr.charAt(2) == '|') {
                    throw new RuntimeException("Не удалось распознать содержимое файла");
                } else {
                    nonTerminals.add(matcher.group(1).charAt(0));
                    commands.add(new Command(
                            new FLeft(s0, emptySym, matcher.group(1).charAt(0)),
                            new ArrayList<>()));
                    commands.get(commands.size() - 1).values.add(new FRight(s0, ""));
                    for (int i = 0; i < matcher.group(2).length(); i++) {
                        if (matcher.group(2).charAt(i) == '|') {
                            if (matcher.group(2).charAt(i - 1) != '|')
                                commands.get(commands.size() - 1).values.add(new FRight(s0, ""));
                        }
                        else {
                            terminals.add(matcher.group(2).charAt(i));
                            size = commands.get(commands.size() - 1).values.size();
                            commands.get(commands.size() - 1)
                                    .values.get(size - 1)
                                    .c += matcher.group(2).charAt(i);
                        }
                    }
                }
                tmpStr = br.readLine();
            }
            for (Character c : nonTerminals) {
                terminals.remove(c);
            }
            for (Character c : terminals) {
                commands.add(new Command(new FLeft(s0, c, c), new ArrayList<>(){{
                    add(new FRight(s0, "\0"));
                }}));
            }
            commands.add(new Command(new FLeft(s0, emptySym, h0), new ArrayList<>(){{
                add(new FRight(s0, "\0"));
            }}));
        }

        void printData() {
            System.out.print("Входной алфавит:\nP = {");
            for (Character c : terminals) {
                System.out.print(c + ", ");
            }
            System.out.println("\b\b}\n");
            System.out.print("Алфавит магазинных символов:\nZ = {");
            for (Character c : nonTerminals) {
                System.out.print(c + ", ");
            }
            for (Character c : terminals) {
                System.out.print(c + ", ");
            }
            System.out.println("h0}\n");

            System.out.println("Список команд:");
            for (Command c : commands) {
                System.out.print("f(s" + c.fargs.s + ", ");
                if (c.fargs.p == emptySym)
                    System.out.print("lambda");
                else System.out.print(c.fargs.p);
                System.out.print(", ");
                if (c.fargs.h == h0)
                    System.out.print("h0");
                else System.out.print(c.fargs.h);
                System.out.print(") = {");
                for (FRight v : c.values) {
                    System.out.print("(s" + v.s + ", ");
                    if (v.c.charAt(0) == emptySym)
                        System.out.print("lambda");
                    else System.out.print(v.c);
                    System.out.print("); ");
                }
                System.out.println("\b\b}");
            }
            System.out.println();
        }

        void printChain() {
            System.out.println("\nЦепочка конфигураций: ");
            for (Configuration link : chain) {
                System.out.print("(s" + link.s + ", " + (link.input.length() == 0 ? "lambda" : link.input) + ", h0" + link.stack + ") |- ");
            }
            System.out.println("(s0, lambda, lambda)");
        }

        boolean makeChain() {
            int chSize = chain.size();
            int magSize, j, i;
            for (i = 0; i < commands.size(); i++) {
                chain.get(chSize - 1).stack = chain.get(chSize - 1).stack.replaceAll("\u0000", "");
                magSize = chain.get(chSize - 1).stack.length();
                if (chain.get(chain.size() - 1).input.length() != 0
                        && chain.get(chain.size() - 1).stack.length() != 0
                        && chain.get(chSize - 1).s == commands.get(i).fargs.s
                        && (chain.get(chSize - 1).input.charAt(0) == commands.get(i).fargs.p
                            || emptySym == commands.get(i).fargs.p)
                        && chain.get(chSize - 1).stack.charAt(magSize - 1) == commands.get(i).fargs.h) {
                    for (j = 0; j < commands.get(i).values.size(); j++) {
                        if (commands.get(i).fargs.p == emptySym) {
                            chain.add(new Configuration(commands.get(i).values.get(j).s, chain.get(chSize - 1).input, chain.get(chSize - 1).stack));
                        } else {
                            chain.add(new Configuration(commands.get(i).values.get(j).s, chain.get(chSize - 1).input, chain.get(chSize - 1).stack));
                            chain.get(chSize).input = chain.get(chSize).input.substring(1);
                        }

                        chain.get(chSize).stack = chain.get(chSize).stack.substring(0, chain.get(chSize).stack.length() - 1);
                        chain.get(chSize).stack += commands.get(i).values.get(j).c;
                        chain.get(chSize).stack = chain.get(chSize).stack.replaceAll("\u0000", "");

                        if (chain.get(chSize).input.length() < chain.get(chSize).stack.length()) {
                            chain.remove(chain.size()-1);
                            chain.remove(chain.size()-1);
                            return false;
                        }
                        else if (chain.get(chain.size() - 1).input.length() == 0 && chain.get(chain.size() - 1).stack.length() == 0 || makeChain()) {
                                return true;
                        }
                    }
                }
            }
            if (i == commands.size()) {
                chain.remove(chain.size()-1);
                return false;
            }
            return false;
        }

        void testInput(String str) {
            chain.add(new Configuration(s0, str, commands.get(0).fargs.h + ""));

            boolean res = makeChain();
            if (res) {
                System.out.println("Допустимая строка");
                printChain();
            } else {
                System.out.println("Недопустимая строка");
            }
            chain.clear();
        }
    }

    public static void main(String[] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String str;
        try {
            ComBuilder strg = new ComBuilder("src/test1.txt");
            strg.printData();
            while (true)
            {
                System.out.println("Введите строку: ");
                str = br.readLine();
                if (Objects.equals(str, "EXIT")) break;
                strg.testInput(str);
                System.out.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
