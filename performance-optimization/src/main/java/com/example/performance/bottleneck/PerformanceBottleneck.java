package com.example.performance.bottleneck;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 실습 1: 성능 병목 코드 분석
 * 
 * 이 클래스는 성능 병목이 있는 코드 예제입니다.
 * 각 메서드의 문제점을 찾고 개선해보세요.
 */
public class PerformanceBottleneck {
    
    /**
     * 문제 1: 비효율적인 문자열 연결
     * 매번 새로운 String 객체를 생성하여 성능 저하 발생
     */
    public String concatenateStrings(List<String> strings) {
        String result = "";
        for (String str : strings) {
            result += str;  // 매번 새로운 String 객체 생성
        }
        return result;
    }
    
    /**
     * 개선된 버전: StringBuilder 사용
     */
    public String concatenateStringsOptimized(List<String> strings) {
        StringBuilder sb = new StringBuilder();
        for (String str : strings) {
            sb.append(str);
        }
        return sb.toString();
    }
    
    /**
     * 문제 2: 중첩 루프의 비효율
     * 시간 복잡도: O(n*m)
     */
    public List<Integer> findDuplicates(List<Integer> list1, List<Integer> list2) {
        List<Integer> duplicates = new ArrayList<>();
        for (Integer num1 : list1) {
            for (Integer num2 : list2) {
                if (num1.equals(num2)) {
                    duplicates.add(num1);
                    break;
                }
            }
        }
        return duplicates;
    }
    
    /**
     * 개선된 버전: HashSet을 활용한 O(1) 조회
     * 시간 복잡도: O(n+m)
     */
    public List<Integer> findDuplicatesOptimized(List<Integer> list1, List<Integer> list2) {
        Set<Integer> set2 = new HashSet<>(list2);
        List<Integer> duplicates = new ArrayList<>();
        for (Integer num1 : list1) {
            if (set2.contains(num1)) {
                duplicates.add(num1);
            }
        }
        return duplicates;
    }
    
    /**
     * 문제 3: 불필요한 객체 생성
     * 루프 내에서 매번 새로운 Date 객체 생성
     */
    public void processData(List<String> data) {
        for (int i = 0; i < data.size(); i++) {
            String item = data.get(i);
            // 매번 새로운 Date 객체 생성
            Date now = new Date();
            System.out.println(now + ": " + item);
        }
    }
    
    /**
     * 개선된 버전: 루프 밖으로 객체 생성 이동
     */
    public void processDataOptimized(List<String> data) {
        Date now = new Date();  // 루프 밖에서 한 번만 생성
        for (String item : data) {
            System.out.println(now + ": " + item);
        }
    }
}
