package problems;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IpAddress {

    private static final String IP_PATTERN = "^(\\d+\\.\\d+\\.\\d+\\.\\d+).*";

    public static void main(String[] args) {

        String[] test = {
                "1.0.0.1 - sdjkfhs",
                "1.0.0.1 - sdjkfhs",
                "1.0.23.1 - sdjk sd dfhs",
                "1.0.23.1 - sdjk sd",
                "1.1.1.1 - sdjkfhs"
        };


        Optional<String> result = findFirstMostCommonIp(test);

        System.out.println(result);
    }


    static Stream<String> extractIps(String[] input) {
        return Arrays.stream(input)
                .map(str -> str.replaceFirst(IP_PATTERN, "$1"));
    }

    static Map<String, Long> groupByIp(Stream<String> ips) {
        return ips.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    static Optional<String> findMostCommonIp(String[] input) {
        return groupByIp(extractIps(input))
                .entrySet()
                .stream()
                .max(Comparator.comparing(e -> e.getValue()))
                .map(s -> s.getKey());
    }


    static Optional<String> findFirstMostCommonIp(String[] input) {

        Map<String, Long> groups = groupByIp(extractIps(input));

        Optional<Long> maxCount = groups.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(s -> s.getValue());

        return maxCount.map(n -> filterForKeysWithValue(groups, n))
                .flatMap(mostCommonIps -> extractIps(input)
                    .filter(ip -> mostCommonIps.anyMatch(ip::equals))
                    .findFirst());
    }

    static Stream<String> filterForKeysWithValue(Map<String, Long> entries, Long value) {
        return entries.entrySet()
                .stream()
                .filter(e -> e.getValue().equals(value))
                .map(e -> e.getKey());
    }

}
