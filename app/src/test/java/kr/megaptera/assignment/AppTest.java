package kr.megaptera.assignment;

import org.junit.jupiter.api.Test;

class AppTest {

    @Test
    void test() {

        String path = "/tasks/";
        String[] splits = path.split("/");
        for (String str : splits) {
            System.out.println("str = " + str);
        }

        Long sequence = 0L;
        System.out.println("sequence = " + sequence);

        ++sequence;

        System.out.println("sequence = " + sequence);
    }
}
