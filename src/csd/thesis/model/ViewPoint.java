package csd.thesis.model;

import edu.stanford.nlp.util.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ViewPoint extends ArrayList<Pair> {
    private Map<Pair<String, String>, Integer> pairs = new HashMap<>();
    private Map<String, Integer> vp  = new HashMap<>();

    public boolean add(String neA, String neB) {
        if (neA.equals(neB))
            return false;

        Pair<String,String> elem = new Pair<String,String>(neA, neB);
        Pair<String,String> opp_elem = (Pair<String,String>) this.getOppositePair(elem);
        if (!pairs.containsKey(elem)) {
            if (!this.containsPair(elem, true))
                pairs.put(elem, 1);
        } else {
            pairs.put(elem, pairs.get(elem) + 1);
        }
        return super.add(elem);
    }

    public void addTokensfromSentence(ArrayList<String> sentence) {
        sentence.forEach(tokenA -> {
            sentence.forEach(tokenB -> {
                add((String) tokenA, (String) tokenB);
            });
        });
    }
    public void addTokenPairsfromSentence(ArrayList<String> sentence) {
        sentence.forEach(tokenA -> {
            sentence.forEach(tokenB -> {
                add((String) tokenA, (String) tokenB);
            });
        });
    }

    public boolean containsPair(Pair o, boolean opposite) {
        String a, b;
        a = (opposite) ? (String) o.second : (String) o.first;
        b = (opposite) ? (String) o.first : (String) o.second;
        return this.stream().anyMatch(elem -> elem.first.equals(a) && elem.second.equals(b));
    }

    public Pair<String, String> getOppositePair(Pair<String, String> o) {
        return new Pair<String,String>((String) o.second, (String) o.first);
    }

    public int getCount(String neA, String neB) {
        Pair<String,String> elem = new Pair<String,String>(neA, neB);

        if (!pairs.containsKey(elem)) {
            return 0;
        }
        return pairs.get(elem);
    }

    public int getCount(Pair elem) {
        if (!pairs.containsKey(elem)) {
            return 0;
        }
        return pairs.get(elem);
    }

    public Map<Pair<String, String>, Integer> getPairs() {
        return this.pairs;
    }

    public Map<Pair<String, String>, Integer> getPairsSortedByValue() {
        return this.sortByValue();
    }

    private Map<Pair<String, String>, Integer> sortByValue() {

        List<Map.Entry<Pair<String, String>, Integer>> list = new ArrayList<>(pairs.entrySet());
        Comparator<Map.Entry<Pair<String, String>, Integer>> cmp = Map.Entry.comparingByValue();
        list.sort(cmp.reversed());

        Map<Pair<String, String>, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<Pair<String, String>, Integer> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

}
