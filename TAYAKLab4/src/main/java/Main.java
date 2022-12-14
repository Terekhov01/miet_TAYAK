import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        SyntaxAnalyzer syntaxAnalyzer = new SyntaxAnalyzer("src/main/resources/grammar.txt");
        try {
            syntaxAnalyzer.call("src/main/resources/cfile.txt");
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
