package ru.mephi.curvestovector;

import java.util.ArrayList;

public class ConvertVector {

    private double maxY;
    private int height;
    private double maxX;
    private int width;
    private int time;
    private int [] kvants = {200000, 100000, 50000, 20000, 10000, 5000, 2000, 1000,
            200, 100, 50};

    public ConvertVector(double maxY, int height, double maxX, int width ) {

        this.maxY = maxY;
        this.height = height;
        this.maxX = maxX;
        this.width = width;
    }

    public ArrayList<String> getVectors(ArrayList<Segment> segments) {
        ArrayList<Vector> vectors = new ArrayList<>();
        for (Segment segment : segments) {
            LineSegment lineSegment = (LineSegment) segment;
            vectors.addAll(getVectorsSegment(lineSegment));
        }
        return getStrFromVector(vectors);
    }

    private ArrayList<String> getStrFromVector(ArrayList<Vector> vectors) {
        ArrayList<String> strings = new ArrayList<>();
        final int last = vectors.size() -1;
        for (int i = 0;i < vectors.size();i++) {
            Vector vector = vectors.get(i);
            StringBuilder sb = new StringBuilder();
            String dur = Integer.toBinaryString(vector.amountSteps-1);
            int m;
            String bef;
            if (i == 0) {
                bef = "01";
            } else if(i == last) {
                bef = "10";
            } else {
                bef = "11";
            }
            int stp;
            int kvant = vector.kvant;
            if (kvant / 1000 != 0) {
                m = 1;
                stp = kvant/1000;
            } else {
                m = 0;
                stp = kvant;
            }
            sb.append(format(dur,8,false));
            sb.append(format("",16,false));
            sb.append(format(Integer.toBinaryString(vector.getCStepHigh()),8,vector.isNegStep()));
            sb.append(format(Integer.toBinaryString(vector.getCStepLow()),8,vector.isNegStep()));
            sb.append(format(Integer.toBinaryString(vector.getCurHigh()),8,false));
            sb.append(format(Integer.toBinaryString(vector.getCurLow()),8,false));
            sb.append("0");
            sb.append(format(getStep(stp),3,false));
            sb.append(m);
            sb.append("0");
            if (i == 0) {
                sb.append("01");
            } else if (i == last) {
                sb.append("10");
            } else {
                sb.append("11");
            }
            strings.add(getHexString(sb.toString()));
        }
        return strings;
    }


    private String getHexString(String binarString) {
        if (binarString.length() != 64) {
            throw new RuntimeException("<64 :" + binarString.length());
        }
        String hex = "";
        for (int i = 0; i < 16; i++) {
            String str = binarString.substring(i * 4,i * 4 + 4);
            int binarInt = Integer.parseInt(str,2);
            hex += Integer.toHexString(binarInt);
        }
        return hex.toUpperCase();
    }



    private ArrayList<Vector> getVectorsSegment(LineSegment segments)  {
        ArrayList<Vector> vectors = new ArrayList<>();
        int l = segments.getDuration();
        for (int i = 0;i < kvants.length;i++) {
            if (l >= kvants[i]) {
                int n = l / kvants[i];
                double ik = segments.getIk(n*kvants[i]);
                int in = segments.getStart().y;
                double iInc = (ik - in)/n;
                Vector v = new Vector(n,iInc,in,kvants[i],maxY,height);
                if (v.getCStepHigh() >= 127 || v.getCStepLow() >= 127 || v.getCurHigh() >= 250 || v.getCurLow() >= 250) {
                    continue;
                }
                vectors.add(v);
                int ln = l - n*kvants[i];
                vectors.addAll(getVectorsSegment(new LineSegment(segments,ln,false)));
                break;
            }
        }
        return vectors;
    }


    private String format(String str, int n, boolean isNeg) {
        String newstr = "";
        if (str.length() > n) {
            throw new RuntimeException("Вектор невозможно сделать. Больше 256 шагов: " + str.length());
        }
        if (str.length() < n) {
            for (int i = 0; i < n - str.length();i++) {
                newstr +="0";
            }
        }
        newstr+=str;
        if (isNeg) {
            newstr = convertAddCode(newstr);
        }
        return newstr;
    }

    private String convertAddCode(String str) {
        char[] chars =  str.toCharArray();
        for (int i = 0;i < chars.length;i++) {
            char s = chars[i];
            if (s == '0') {
                chars[i] = '1';
            } else  {
                chars[i] = '0';
            }
        }
        String string = String.valueOf(chars);
        int i = Integer.parseInt(string,2);
        if (i > 255) {
           throw new RuntimeException();
        }
        i++;
        String addCode = Integer.toBinaryString(i);
        if (addCode.length() != 8) {
            return "00000000";
        }
        return Integer.toBinaryString(i);
    }

    class Vector{
        private int amountSteps;
        private double cStep;
        private double cur;
        private int kvant;
        private double maxY;
        private int height;

        public Vector(int amountSteps, double cStep, double cur, int kvant, double maxY, int height) {
            this.amountSteps = amountSteps;
            this.cStep = cStep;
            this.cur = cur;
            this.kvant = kvant;
            this.maxY = maxY;
            this.height = height;
        }

        public int getAmountSteps() {
            return amountSteps;
        }

        public int getKvant() {
            return kvant;
        }

        public int getCStepHigh() {
            double ost = (cStep % 100) % 16;
            int value = (int) Math.abs(Math.round ((cStep - ost) * 126/(maxY/2)));
            return value;
        }

        public int getCStepLow() {
            int value = (int) Math.abs(Math.round(((cStep % 100) % 16) * 126/(8)));
            return value;
        }

        public int getCurHigh() {
            double ost = (cur % 100) % 16;
            int value = (int) Math.abs(Math.round((cur - ost) * 249/(maxY)));
            return value;
        }

        public int getCurLow() {
            int value = (int) Math.abs(Math.round(((cur %100)%16) * 249/(16)));
            return value;
        }

        public boolean isNegStep() {
            return cStep < 0;
        }
    }

    private String getStep(int step) {
        String str = "";
        switch (step) {
            case(1):
                str = "000";
                break;
            case(2):
                str = "001";
                break;
            case(5):
                str = "010";
                break;
            case(10):
                str = "011";
                break;
            case(20):
                str = "100";
                break;
            case(50):
                str = "101";
                break;
            case(100):
                str = "110";
                break;
            case(200):
                str = "111";
                break;
            default:
                throw new RuntimeException("Incorrect step value");
        }
        return str;
    }
}
