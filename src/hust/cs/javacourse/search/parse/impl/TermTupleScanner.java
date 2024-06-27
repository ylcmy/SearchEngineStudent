package hust.cs.javacourse.search.parse.impl;

import hust.cs.javacourse.search.index.AbstractTermTuple;
import hust.cs.javacourse.search.index.impl.Term;
import hust.cs.javacourse.search.index.impl.TermTuple;
import hust.cs.javacourse.search.parse.AbstractTermTupleScanner;
import hust.cs.javacourse.search.util.Config;
import hust.cs.javacourse.search.util.StringSplitter;

import java.io.BufferedReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class TermTupleScanner extends AbstractTermTupleScanner {
    private Queue<AbstractTermTuple> tuples = new LinkedList<>();
    private int pos;
    private StringSplitter splitter;

    /**
     * 缺省构造函数
     */
    public TermTupleScanner() {
    }

    /**
     * 构造函数
     *
     * @param input：指定输入流对象，应该关联到一个文本文件
     */
    public TermTupleScanner(BufferedReader input) {
        super(input);
        splitter = new StringSplitter();
        splitter.setSplitRegex(Config.STRING_SPLITTER_REGEX);
        pos = 0;
    }

    /**
     * 获得下一个三元组
     *
     * @return: 下一个三元组；如果到了流的末尾，返回null
     */
    @Override
    public AbstractTermTuple next() {
        if (tuples.size() == 0) {
            try {
                String str = input.readLine();
                while (str != null && str.equals("")) {
                    str = input.readLine();
                }
                if (str != null) {
                    List<String> words;
                    if (Config.IGNORE_CASE)
                        words = splitter.splitByRegex(str.toLowerCase());
                    else
                        words = splitter.splitByRegex(str);
                    for (String word : words) {
                        tuples.add(new TermTuple(new Term(word), pos++));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return tuples.poll();
    }
}
