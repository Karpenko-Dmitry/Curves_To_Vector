package ru.mephi.curvestovector;

import java.util.ArrayList;
import java.util.Collections;

public class VectorConvert {

    private double maxY;
    private int height;
    private double maxX;
    private int width;
    private ArrayList<Path> mPaths;
    private int [] kvants = {200000, 100000, 50000, 20000, 10000, 5000, 2000, 1000,
                     200, 100, 50};

    public VectorConvert(double maxY, int height, double maxX, int width ) {
        mPaths = new ArrayList<>();
        this.maxY = maxY;
        this.height = height;
        this.maxX = maxX;
        this.width = width;
    }

    public ArrayList<String> getVectors(ArrayList<Segment> segments) {
        ArrayList<LineSegment> lineSegments = castLineSegment(segments);
        for (int s = 0; s < lineSegments.size();s++) {
            LineSegment segment = lineSegments.get(s);
            for (int i = 0; i < kvants.length ;i++) {
                mPaths.add(new Path(s,kvants[i], getLineVectorsPath(segment,kvants[i])));
            }
        }
        ArrayList<Integer> index = findBestPath(lineSegments.size());
        ArrayList<String> strings = new ArrayList<>();
        if (index.size() == lineSegments.size()) {
            for (int i = 0; i < index.size(); i++) {
                LineSegment segment = lineSegments.get(i);
                getLineVectors(segment,mPaths.get(i).kvant,strings,i);
            }
        } else {
           throw new RuntimeException("index.size() != lineSegments.size()");
        }
        return strings;
    }

    private ArrayList<LineSegment> castLineSegment(ArrayList<Segment> segments) {
        ArrayList<LineSegment> lineSegments = new ArrayList<>();
        for (Segment segment : segments) {
            lineSegments.add((LineSegment) segment);
        }
        return lineSegments;
    }

    private void getLineVectors(LineSegment segment, int kvant, ArrayList<String> strings,int number) {
        int step;
        int duration = getRealDuration(segment.getDuration());
        if (duration <= 0) {
            return;
        }
        if (number != 1 && number != -1) {
            number = 3;
        } else if (number == -1) {
            number = 2;
        }
        if (kratnoe(duration,kvant)) {
            step = duration / kvant;
            int incI = getIntInc(segment.getIncrease(height,maxY) / step);
            if (verify(incI)) {
                strings.add(getVectorFromSegment(step,getHighI(segment.getIncrease(height,maxY) / step),
                        getLowI(segment.getIncrease(height,maxY) / step),getIntHighI(segment.getStart().y),
                        getIntLowI(segment.getStart().y),kvant,number));
                return;
            } else {
                getLineVectors(segment,getNextKvant(kvant),strings,number);
                return;
            }
        } else {
            int n = duration / kvant;
            if (n > 0) {
                int incI = getIntInc((segment.getIncrease(height,maxY)) / n);
                int k1,k2;
                if (verify(incI)) {
                    strings.add(getVectorFromSegment(n,getHighI(segment.getIncrease(height,maxY) / n),
                            getLowI(segment.getIncrease(height,maxY) / n),getIntHighI(segment.getStart().y),
                            getIntLowI(segment.getStart().y),kvant,number));
                } else {
                    getLineVectors(new LineSegment(segment,n * kvant,true), getNextKvant(kvant),strings,number);
                }
                int tдл1 = duration - n * kvant;
                getLineVectors(new LineSegment(segment,tдл1,false), getNextKvant(kvant),strings,number);
                return;
            } else {
                getLineVectors(segment,getNextKvant(kvant),strings,number);
                return;
            }
        }
    }

    private int getRealDuration(int duration) {
        return (int) (duration * 1000000 * maxX / width);
    }

    private int getIntInc(double i) {
        return (int) (256 * i / maxY);
    }

    private int getHighI(double inc) {
        int microInc = (int) (inc * 1000000);
        return (microInc / 16000) - 1;
    }

    private int getLowI(double inc) {
        int microInc = (int) (inc * 1000000);
        return microInc % 16001;
    }

    private int getIntHighI(int inc) {
        double i = inc * maxY / height;
        int microInc = (int) (i * 1000000);
        return (microInc / 1600) - 1;
    }

    private int getIntLowI(int inc) {
        double i = inc * maxY / height;
        int microInc = (int) (i * 1000000);
        return microInc % 16001;
    }

    private String getVectorFromSegment(int numberStep,int cStepHi,int cStepLow,int highCur,int lowCur,int kvant, int numberSeg) {
        StringBuilder sb = new StringBuilder();
        String dur = Integer.toBinaryString(numberStep-1);
        int m;
        String bef;
        switch (numberSeg) {
            case(0):
                bef = "00";
                break;
            case(1):
                bef = "01";
                break;
            case(2):
                bef = "10";
                break;
            case(3):
                bef = "11";
                break;
            default:
                throw new IllegalArgumentException("numberSeg  " + numberSeg);
        }
        int stp;
        if (kvant / 1000 != 0) {
            m = 1;
            stp = kvant/1000;
        } else {
            m = 0;
            stp = kvant;
        }
        sb.append(format(dur,8));
        sb.append(format("",16));
        sb.append(format(Integer.toBinaryString(cStepHi),8));
        sb.append(format(Integer.toBinaryString(cStepLow),8));
        sb.append(format(Integer.toBinaryString(highCur),8));
        sb.append(format(Integer.toBinaryString(lowCur),8));
        sb.append("0");
        sb.append(format(getStep(stp),3));
        sb.append(m);
        sb.append("0");
        String hex = Integer.toHexString(Integer.parseInt(sb.toString(),2));
        return hex;
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

    private String format(String str, int n) {
        String newstr = "";
        if (str.length() > n) {
            throw new RuntimeException("Вектор невозможно сделать. Больше 256 шагов");
        }
        if (str.length() < n) {
           for (int i = 0; i < n - str.length();i++) {
               newstr +="0";
           }
        }
        newstr+=str;
        return newstr;
    }


    private int getFirstbySegmentNumber(int s) {
        for (int i = 0; i < mPaths.size();i++) {
            if (mPaths.get(i).segmentNumber == s) {
                return i;
            }
        }
        return -1;
    }

    private int getLastbySegmentNumber(int s) {
        for (int i = 0; i < mPaths.size();i++) {
            if (mPaths.get(i).segmentNumber == s && mPaths.get((i + 1) % mPaths.size()).segmentNumber != s) {
                return i;
            }
        }
        if (mPaths.get(mPaths.size()-1).segmentNumber == s) {
            return mPaths.size()-1;
        }
        return -1;
    }

    private ArrayList<Integer> findBestPath(int size) {
        ArrayList<Integer> index = new ArrayList<>();
        for (int s = 0; s < size; s++) {
            ArrayList<Path> segPath = (ArrayList<Path>) mPaths.subList(getFirstbySegmentNumber(s),getLastbySegmentNumber(s));
            Path p = Collections.min(segPath,(c1,c2) -> c1.amount - c2.amount);
            index.add(mPaths.indexOf(p));
        }
        return index;
    }

    private int getLineVectorsPath(LineSegment segment, int kvant) {
        int step;
        int duration = getRealDuration(segment.getDuration());
        if (duration <= 0) {
            return -1;
        }
        if (kratnoe(duration,kvant)) {
            step = duration / kvant;
            int incI = getIntInc((segment.getIncrease(height,maxY)) / step);
            if (verify(incI)) {
                return 1;
            } else {
                return getLineVectorsPath(segment,getNextKvant(kvant));
            }
        } else {
            int n = duration / kvant;
            if (n > 0) {
                int incI = getIntInc((segment.getIncrease(height,maxY)) / n);
                int k1,k2;
                if (verify(incI)) {
                    k1 = 1;
                } else {
                    k1 = getLineVectorsPath(new LineSegment(segment,n * kvant,true), getNextKvant(kvant));
                }
                int tдл1 = duration - n * kvant;
                k2 = getLineVectorsPath(new LineSegment(segment,tдл1,false), getNextKvant(kvant));
                return k1 + k2;
            } else {
                int k = getLineVectorsPath(segment,getNextKvant(kvant));
                return k;
            }
        }
    }


    private boolean kratnoe(int tдл, int kvant) {
        return tдл >= kvant && tдл % kvant == 0;
    }

    private boolean verify(int incI) {
        return incI >= -127 && incI <= 126;
    }


    private int getNextKvant( int kvant) {
        for (int i = 0; i < kvants.length; i++) {
            if (kvants[i] == kvant) {
                if (i + 1 < kvants.length) {
                   return kvants[i+1];
                } else {
                    return 50;
                }
            }
        }
        return 1;
    }

    class Path {
        private int segmentNumber;
        private int kvant;
        private int amount;

        public Path(int segmentNumber, int kvant, int amount) {
            this.segmentNumber = segmentNumber;
            this.kvant = kvant;
            this.amount = amount;
        }

    }

}
