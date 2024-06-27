package hust.cs.javacourse.search.query.impl;

import hust.cs.javacourse.search.index.AbstractPosting;
import hust.cs.javacourse.search.index.AbstractPostingList;
import hust.cs.javacourse.search.index.AbstractTerm;
import hust.cs.javacourse.search.index.impl.PostingList;
import hust.cs.javacourse.search.query.AbstractHit;
import hust.cs.javacourse.search.query.AbstractIndexSearcher;
import hust.cs.javacourse.search.query.Sort;

import java.io.File;
import java.util.*;

public class IndexSearcher extends AbstractIndexSearcher {
    /**
     * 从指定索引文件打开索引，加载到index对象里. 一定要先打开索引，才能执行search方法
     * @param indexFile ：指定索引文件
     */
    @Override
    public void open(String indexFile) {
        index.load(new File(indexFile));
    }

    /**
     * 根据单个检索词进行搜索
     * @param queryTerm ：检索词
     * @param sorter ：排序器
     * @return ：命中结果数组
     */
    @Override
    public AbstractHit[] search(AbstractTerm queryTerm, Sort sorter) {
        PostingList list = (PostingList)index.search(queryTerm);
        if(list == null)
            return null;
        AbstractHit[] hits = new AbstractHit[list.size()];
        AbstractHit hit;
        for(int i = 0;i<hits.length;i++){
            AbstractPosting posting = list.get(i);
            Map<AbstractTerm, AbstractPosting> termPostingMapping = new TreeMap<>();
            termPostingMapping.put(queryTerm,posting);
            hit = new Hit(posting.getDocId(),this.index.getDocName(posting.getDocId()),termPostingMapping);
            sorter.score(hit);
            hits[i] = hit;
        }
        sorter.sort(Arrays.asList(hits));
        return hits;
    }

    /**
     *
     * 根据二个检索词进行搜索
     * @param queryTerm1 ：第1个检索词
     * @param queryTerm2 ：第2个检索词
     * @param sorter ：    排序器
     * @param combine ：   多个检索词的逻辑组合方式
     * @return ：命中结果数组
     */
    @Override
    public AbstractHit[] search(AbstractTerm queryTerm1, AbstractTerm queryTerm2, Sort sorter, LogicalCombination combine) {
        PostingList list1 = (PostingList)index.search(queryTerm1);
        PostingList list2 = (PostingList)index.search(queryTerm2);
        List<AbstractHit> hitList = new ArrayList<>();
        if(combine == LogicalCombination.AND){
            if(list1 == null || list2 == null){
                return null;
            }
            int i = 0, j = 0;
            AbstractPosting posting1,posting2;
            while(i < list1.size() && j < list2.size()){
                posting1 = list1.get(i);
                posting2 = list2.get(j);
                if(posting1.getDocId() == posting2.getDocId()){
                    int docId = posting1.getDocId();
                    Map<AbstractTerm, AbstractPosting> termPostingMapping = new TreeMap<>();
                    termPostingMapping.put(queryTerm1,posting1);
                    termPostingMapping.put(queryTerm2,posting2);
                    AbstractHit hit = new Hit(docId,index.getDocName(docId),termPostingMapping);
                    sorter.score(hit);
                    hitList.add(hit);
                    i++;
                    j++;
                } else if(posting1.getDocId() < posting2.getDocId()){
                    i++;
                } else{
                    j++;
                }
            }
            if(hitList.isEmpty())
                return null;
            sorter.sort(hitList);
            AbstractHit[] hits = new AbstractHit[hitList.size()];
            hitList.toArray(hits);
            return hits;
        }

        else if(combine == LogicalCombination.OR){
            if(list1 == null){
                return search(queryTerm2,sorter);
            }
            else if(list2 == null){
                return search(queryTerm1,sorter);
            }
            else{
                int i = 0, j = 0, docId;
                AbstractPosting posting1,posting2;
                while(i < list1.size() && j < list2.size()) {
                    posting1 = list1.get(i);
                    posting2 = list2.get(j);
                    //对于两个关键词都同时命中的情况单独处理
                    if (posting1.getDocId() == posting2.getDocId()) {
                        docId = posting1.getDocId();
                        Map<AbstractTerm, AbstractPosting> termPostingMapping = new TreeMap<>();
                        termPostingMapping.put(queryTerm1, posting1);
                        termPostingMapping.put(queryTerm2, posting2);
                        AbstractHit hit = new Hit(docId, index.getDocName(docId), termPostingMapping);
                        sorter.score(hit);
                        hitList.add(hit);
                        i++;
                        j++;
                    }else if(posting1.getDocId() < posting2.getDocId()){
                        docId = posting1.getDocId();
                        Map<AbstractTerm, AbstractPosting> termPostingMapping = new TreeMap<>();
                        termPostingMapping.put(queryTerm1, posting1);
                        AbstractHit hit = new Hit(docId, index.getDocName(docId), termPostingMapping);
                        sorter.score(hit);
                        hitList.add(hit);
                        i++;
                    } else{
                        docId = posting2.getDocId();
                        Map<AbstractTerm, AbstractPosting> termPostingMapping = new TreeMap<>();
                        termPostingMapping.put(queryTerm2, posting2);
                        AbstractHit hit = new Hit(docId, index.getDocName(docId), termPostingMapping);
                        sorter.score(hit);
                        hitList.add(hit);
                        j++;
                    }
                }
                while( i < list1.size()){
                    posting1 = list1.get(i);
                    docId = posting1.getDocId();
                    Map<AbstractTerm, AbstractPosting> termPostingMapping = new TreeMap<>();
                    termPostingMapping.put(queryTerm1, posting1);
                    AbstractHit hit = new Hit(docId, index.getDocName(docId), termPostingMapping);
                    sorter.score(hit);
                    hitList.add(hit);
                    i++;
                }
                while( j < list2.size()){
                    posting2 = list2.get(j);
                    docId = posting2.getDocId();
                    Map<AbstractTerm, AbstractPosting> termPostingMapping = new TreeMap<>();
                    termPostingMapping.put(queryTerm2, posting2);
                    AbstractHit hit = new Hit(docId, index.getDocName(docId), termPostingMapping);
                    sorter.score(hit);
                    hitList.add(hit);
                    j++;
                }
                if(hitList.isEmpty())
                    return null;
                sorter.sort(hitList);
                AbstractHit[] hits = new AbstractHit[hitList.size()];
                hitList.toArray(hits);
                return hits;
            }
        }
        return null;
    }
}