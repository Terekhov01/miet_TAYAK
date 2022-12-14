import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidParameterException;
import java.util.EmptyStackException;
import java.util.Objects;
import java.util.Stack;

public class Calculator
{
    public enum Functions {
        log, pow;
        public static double calc(Functions f, double a, double b){
            switch (f){
                case log -> {
                    if (a <= 0){
                        System.out.println("Основание логарифма должно быть положительным");
                        return Double.NaN;
                    }
                    if (a == 1){
                        System.out.println("Основание логарифма должно отличаться от 1");
                        return Double.NaN;
                    }
                    if (b < 0){
                        System.out.println("Число должно быть положительным");
                        return Double.NaN;
                    }
                    return Math.log(a)/Math.log(b);
                }
                case pow -> {
                    return Math.pow(a, b);
                }
            }
            throw new InvalidParameterException("Калькулятор не работает с введенной функцией");
        }
    }

    private Functions function = Functions.log;
    private String input;

    public Calculator() throws IOException {
        while (true)
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Введите выражение: ");
            input = reader.readLine();
            input = input.replaceAll("[\\s\\t]", "");
            if (Objects.equals(input, "")) break;

            System.out.println(calculate());
        }
    }

    public void setFunction(Functions function) {
        this.function = function;
    }

    private boolean isOperator(char symbol) {
        return rank(symbol) < 4 || symbol == 'f';
    }

    private boolean isSeparator(char c)
    {
        if (c == ' ' || c=='\t')
            return true;
        return false;
    }


    private int rank(char symbol)
    {
        return switch (symbol) {
            case '(', ')' -> 1;
            case '+', '-' -> 2;
            case '*', '/' -> 3;
            default -> 4;
        };
    }

    private void getExpression()
    {
        input = input.replace(".", ",");
        while (input.contains(function.toString()))
            input = input.replaceAll(function.toString()+"\\((.*?),(.*?)\\)","(($1)f($2))");
        if (input.contains(",,"))
            System.out.println("Некорректный ввод!");

        StringBuilder output = new StringBuilder();
        Stack<Character> operatorsStack = new Stack<>();
        for (int i = 0; i < input.length(); i++)
        {
            if (isSeparator(input.charAt(i)))
                continue;
            if (Character.isDigit(input.charAt(i)) || input.charAt(i) == ',')
            {
                StringBuilder tmpOut = new StringBuilder();
                while (!isOperator(input.charAt(i)) && !isSeparator(input.charAt(i)))
                {
                    if (!Character.isDigit(input.charAt(i)) && input.charAt(i) != ',')
                        System.out.println(i + 1 + " символ не является оператором или цифрой");
                    tmpOut.append(input.charAt(i));
                    i++;
                    if (i == input.length()) break;
                }
                output.append(tmpOut).append(" ");
                i--;
            }

            else if (isOperator(input.charAt(i)))
            {
                if (i + 1 < input.length())
                    if (input.charAt(i) == '(' && input.charAt(i+1) == '-')
                        output.append("0");

                if (input.charAt(i) == '(')
                    operatorsStack.push(input.charAt(i));

                else if (input.charAt(i) == ')')
                {
                    try
                    {
                        char s = operatorsStack.pop();

                        while (s != '(')
                        {
                            output.append(s).append(" ");
                            s = operatorsStack.pop();
                        }
                    }
                    catch (EmptyStackException e)
                    {
                        System.out.println("Ошибка! Неправильно расставлены скобки!");
                    }
                }
                else
                {
                    if (operatorsStack.size() > 0)
                        if (rank(input.charAt(i)) <= rank(operatorsStack.peek()))
                            output.append(operatorsStack.pop()).append(" ");

                    operatorsStack.push(input.charAt(i));

                }
            }
            else System.out.println((i + 1) + " символ не является оператором или цифрой");
        }


        while (operatorsStack.size() > 0)
            output.append(operatorsStack.pop()).append(" ");

        input = String.valueOf(output);
    }

    private double counting()
    {
        double result = Double.NaN;
        Stack<Double> tmp = new Stack<>();

        for (int i = 0; i < input.length(); i++)
        {
            if (Character.isDigit(input.charAt(i))
                    || input.charAt(i) == ',')
            {
                String a = "";

                while (!isOperator(input.charAt(i)) && !isSeparator(input.charAt(i)))
                {
                    a += input.charAt(i);
                    i++;
                    if (i == input.length()) break;
                }
                a = a.replace(",", ".");
                tmp.push(Double.valueOf(a));
                i--;
            }
            else if (isOperator(input.charAt(i)))
            {
                //два последних значения из стека
                try
                {
                    double a = tmp.pop();
                    double b;
                    if (input.charAt(i) == '-' && tmp.size() == 0)
                        b = 0;
                    else
                        b = tmp.pop();

                    switch (input.charAt(i))
                    {
                        case '+' -> result = b + a;
                        case '-' -> result = b - a;
                        case '*' -> result = b * a;
                        case 'f' -> result = Functions.calc(function, a, b);
                        case '/' -> {
                            if (a == 0) {
                                System.out.println("Деление на ноль!");
                                return Double.NaN;
                            } result = b / a;
                        }
                    }
                    tmp.push(result); //Результат вычисления записываем обратно в стек
                }
                catch (EmptyStackException e){
                    System.out.println("Неверное количество служебных символов!");
                    return Double.NaN;
                }
                catch (InvalidParameterException e){
                    System.out.println(e.getMessage());
                    return Double.NaN;
                }
            }
        }
        try {
            return tmp.pop();
        } catch (EmptyStackException e){
            System.out.println("Нет операций");
            return Double.NaN;
        }
    }

    private double calculate()
    {
        getExpression();
        System.out.println("После преобразования: " + input.replace("f", "log"));
        return counting();
    }

    public static void main(String[] args) throws IOException {
        Calculator calculator = new Calculator();
    }
}
