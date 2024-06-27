package hust.cs.javacourse.search.index.impl;

import hust.cs.javacourse.search.index.*;

import java.io.*;
import java.util.*;

/**
 * AbstractIndex的具体实现类
 */
public class Index extends AbstractIndex {
    /**
     * 返回索引的字符串表示
     *
     * @return 索引的字符串表示
     */
    @Override
    public String toString() {
        return "{\n" +
                "\"docIdToDocPathMapping\" : {\n"+docIdToDocPathMapping+"},\n"+
                "\"termToPostingListMapping\" : {\n" + termToPostingListMapping +"}\n"+
                '}';
    }

    /**
     * 添加文档到索引，更新索引内部的HashMap
     *
     * @param document ：文档的AbstractDocument子类型表示
     */
    @Override
    public void addDocument(AbstractDocument document) {
        this.docIdToDocPathMapping.put(document.getDocId(),document.getDocPath());
        Map<AbstractTerm, List<Integer>> termToPosition=new HashMap<>();
        for (AbstractTermTuple tuple:document.getTuples())
        {
            if(termToPosition.get(tuple.term)==null)
                termToPosition.put(tuple.term,new ArrayList<Integer>());

        termToPosition.get(tuple.term).add(tuple.curPos);
        }
        for(AbstractTerm term:termToPosition.keySet()){
            if(this.termToPostingListMapping.get(term)==null){
                this.termToPostingListMapping.put(term,new PostingList());
            }
            this.termToPostingListMapping.get(term).add(
                    new Posting(document.getDocId(),termToPosition.get(term).size(),termToPosition.get(term))
            );
        }
    }

    /**
     * <pre>
     * 从索引文件里加载已经构建好的索引.内部调用FileSerializable接口方法readObject即可
     * @param file ：索引文件
     * </pre>
     */
    @Override
    public void load(File file) {
        try {
            ObjectInputStream inputStream=new ObjectInputStream(new FileInputStream(file));
            readObject(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * <pre>
     * 将在内存里构建好的索引写入到文件. 内部调用FileSerializable接口方法writeObject即可
     * @param file ：写入的目标索引文件
     * </pre>
     */
    @Override
    public void save(File file) {
        try {
            ObjectOutputStream outputStream=new ObjectOutputStream(new FileOutputStream(file));
            writeObject(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 返回指定单词的PostingList
     *
     * @param term : 指定的单词
     * @return ：指定单词的PostingList;如果索引字典没有该单词，则返回null
     */
    @Override
    public AbstractPostingList search(AbstractTerm term) {
        return this.termToPostingListMapping.get(term);
    }

    /**
     * 返回索引的字典.字典为索引里所有单词的并集
     *
     * @return ：索引中Term列表
     */
    @Override
    public Set<AbstractTerm> getDictionary() {
        return this.termToPostingListMapping.keySet();
    }

    /**
     * <pre>
     * 对索引进行优化，包括：
     *      对索引里每个单词的PostingList按docId从小到大排序
     *      同时对每个Posting里的positions从小到大排序
     * 在内存中把索引构建完后执行该方法
     * </pre>
     */
    @Override
    public void optimize() {
        for (AbstractPostingList postingList:this.termToPostingListMapping.values()){
            postingList.sort();
            for (int i=0;i<postingList.size();i++){
                AbstractPosting posting=postingList.get(i);
                posting.sort();
            }
        }
    }

    /**
     * 根据docId获得对应文档的完全路径名
     *
     * @param docId ：文档id
     * @return : 对应文档的完全路径名
     */
    @Override
    public String getDocName(int docId) {
        return this.docIdToDocPathMapping.get(docId);
    }

    /**
     * 写到二进制文件
     *
     * @param out :输出流对象
     */
    @Override
    public void writeObject(ObjectOutputStream out) {
        try{
            out.writeObject(this.termToPostingListMapping);
            out.writeObject(this.docIdToDocPathMapping);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * 从二进制文件读
     *
     * @param in ：输入流对象
     */
    @Override
    public void readObject(ObjectInputStream in) {
        try{
            this.termToPostingListMapping= (Map<AbstractTerm, AbstractPostingList>) in.readObject();
            this.docIdToDocPathMapping= (Map<Integer, String>) in.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
