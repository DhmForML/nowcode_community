package com.newcode.community.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    private static final String REPLACEMENT = "***";

    private TrieNode rootNode = new TrieNode();


    /**
     * 初始化前缀树
     */
    @PostConstruct
    private void init(){
        try(
                InputStream ins = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
        ) {
                String keyWord;
                while ((keyWord = reader.readLine()) != null){
                        this.addKeyWord(keyWord);
                }
        }catch (IOException e){
            logger.error("加载敏感词文件失败: "+e.getMessage());
        }
    }

    /**
     * 将一个敏感词加入到前缀树中
     * @param keyWord
     */
    private void addKeyWord(String keyWord){
        TrieNode tempNode = rootNode;
        for(int i=0;i<keyWord.length();i++){
            char c = keyWord.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);

            if(subNode == null){
                //初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c,subNode);
            }
            //指向子节点，进入下一轮循环
            tempNode = subNode;
            //设置结束标识
            if(i == keyWord.length() - 1){
                subNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * 对文本中的敏感词进行过滤
     * @param text 待过滤的敏感词
     * @return 过滤之后的敏感词
     */
    public String filter(String text){
        if(StringUtils.isBlank(text)){
            return null;
        }
        TrieNode tempNode = rootNode;
        int begin = 0;
        int position = 0;
        StringBuilder sb = new StringBuilder();
        while (position < text.length()){
            char c = text.charAt(position);
            //跳过符号
            if(isSymbol(c)){
                //若指针1处于根节点，将此符号计入结果，让指针2向后走一步
                if(tempNode == rootNode){
                    begin++;
                    sb.append(c);
                }
                //无论符号在开头或中间，指针3都向后走一步
                position++;
                continue;
            }
            //检查下级节点
            tempNode = tempNode.getSubNode(c);
            if(tempNode == null){
                //以begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                //进入下一个位置
                position = ++begin;
                //重新指向根节点
                tempNode = rootNode;
            }else if (tempNode.isKeywordEnd){
                //发现敏感词，将begin~position字符串替换掉
                sb.append(REPLACEMENT);
                //向后继续判断
                begin = ++position;
                //重新指向根节点
                tempNode = rootNode;
            }else {
                //检查下一个字符
                position++;
            }
        }
        //将最后一批字符计入结果
        sb.append(text.substring(begin));
        return sb.toString();
    }

    /**
     * 判断该字符是否是符号
     * @param c
     * @return
     */
    private boolean isSymbol(Character c){
        //0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    //定义前缀树
    private class TrieNode{
        //关键词结束标识
        private boolean isKeywordEnd = false;

        //字节点(key是下级字符，value是下级节点)
        private Map<Character,TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        public void addSubNode(Character c,TrieNode node){
            subNodes.put(c,node);
        }

        public TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }
    }

}
