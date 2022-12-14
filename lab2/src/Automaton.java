import java.io.*;
import java.util.*;

public class Automaton {
    private Map<AbstractMap.SimpleImmutableEntry<String, Character>, Set<String>> nondeterministicTable =
            new HashMap<>();
    private Map<AbstractMap.SimpleImmutableEntry<String, Character>, String> deterministicTable =
            new HashMap<>();

    private List<String> unconsideredStates = new ArrayList<>();
    private List<String> consideredStates = new ArrayList<>();

    private Set<Character> entryAlphabet = new HashSet<>();

    private String makeState(Set<String> q){
        StringBuilder name = new StringBuilder();
        for (String s : q) {
            name.append(s);
        }
        for (Character character : entryAlphabet) {
            for (String s : q) {
                AbstractMap.SimpleImmutableEntry<String, Character> tmp =
                        new AbstractMap.SimpleImmutableEntry<>(s, character);
                if (nondeterministicTable.containsKey(tmp)){
                    Set<String> tmpSet = nondeterministicTable.get(tmp);
                    AbstractMap.SimpleImmutableEntry<String, Character> tmpEntry =
                            new AbstractMap.SimpleImmutableEntry<>(name.toString(), character);
                    if (nondeterministicTable.containsKey(tmpEntry)){
                        nondeterministicTable.get(tmpEntry).addAll(tmpSet);
                    } else {
                        nondeterministicTable.put(tmpEntry, tmpSet);
                    }
                }
            }
        }
        return name.toString();
    }

    private void print(Map<AbstractMap.SimpleImmutableEntry<String, Character>, String> table){
        for (Map.Entry<AbstractMap.SimpleImmutableEntry<String, Character>, String> elem : table.entrySet()) {
            System.out.println(elem.getKey().getKey() + "," + elem.getKey().getValue() +
                    "=" + elem.getValue());
        }
    }

    private void determine(){
        while(!unconsideredStates.isEmpty()){
            String Qcur = unconsideredStates.get(0);
            int count = 0;
            for (Character character : entryAlphabet) {
                AbstractMap.SimpleImmutableEntry<String, Character> tmp =
                        new AbstractMap.SimpleImmutableEntry<>(Qcur, character);
                if(!nondeterministicTable.containsKey(tmp)) continue;
                Set<String> tmpSet = nondeterministicTable.get(tmp);
                String state = makeState(tmpSet);
                if(!consideredStates.contains(state)){
                    unconsideredStates.add(state);
                }
                deterministicTable.put(tmp, state);
                consideredStates.add(Qcur);
            }
            unconsideredStates.remove(Qcur);
        }
    }

    public void parseAutomate(String fileName){
        String q, f, line;
        char c;

        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
            line = br.readLine();
            q = line.substring(0, line.indexOf(','));
            unconsideredStates.add(q);

            while (line != null && !line.equals(""))
            {
                q = line.substring(0, line.indexOf(','));
                c = line.substring(line.indexOf(',') + 1, line.lastIndexOf('=')).charAt(0);
                entryAlphabet.add(c);
                f = line.substring(line.lastIndexOf('=') + 1);
                AbstractMap.SimpleImmutableEntry<String, Character> tmp= new AbstractMap.SimpleImmutableEntry<>(q, c);
                if (nondeterministicTable.containsKey(tmp)){
                    nondeterministicTable.get(tmp).add(f);
                } else {
                    String finalF = f;
                    nondeterministicTable.put(tmp, new HashSet<>(){{ add(finalF); }});
                }
                line = br.readLine();
            }
            boolean isDeterm = true;
            for (Map.Entry<AbstractMap.SimpleImmutableEntry<String, Character>, Set<String>> elem : nondeterministicTable.entrySet()){
                if (elem.getValue().size() >= 2){
                    isDeterm = false;
                    break;
                }
            }

            if (isDeterm)
            {
                for (Map.Entry<AbstractMap.SimpleImmutableEntry<String, Character>, Set<String>> elem : nondeterministicTable.entrySet()){
                    deterministicTable.put(elem.getKey(), elem.getValue().stream().findFirst().get());
                }
                System.out.println("Automaton is deterministic");
                print(deterministicTable);
            }
            else
            {
                System.out.println("Automaton is non-deterministic");
                determine();
                System.out.println("After determination");
                print(deterministicTable);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private int parseStr(String s){
        String Qcur = "q0";
        int len = s.length();
        for(int i=0; i < len; i++){
            AbstractMap.SimpleImmutableEntry<String, Character> tmp =
                    new AbstractMap.SimpleImmutableEntry<>(Qcur, s.charAt(i));
            if(!deterministicTable.containsKey(tmp)) return -1;
            Qcur = deterministicTable.get(tmp);
            if(Qcur.indexOf('f') != -1 && i == len-1) return 0;
            if(Qcur.indexOf('f') == -1 && i == len-1) return -1;
        }
        return -1;
    }

    public void parsingLoop(){
        String input = "";
        while (true)
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            try {
                input = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            int ret = parseStr(input);
            System.out.println(ret < 0 ? "Not a valid string" : "Valid string");
        }
    }

    public static void main(String[] args) {
        Automaton automate = new Automaton();
        automate.parseAutomate("./src/var3_nd.txt");
    }
}
