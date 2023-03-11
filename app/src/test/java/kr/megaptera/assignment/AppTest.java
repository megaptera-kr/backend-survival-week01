package kr.megaptera.assignment;

import org.junit.jupiter.api.Test;

class AppTest {

    @Test
    void test() {
        Long sequence = 0L;
        System.out.println("sequence = " + sequence);

        ++sequence;
        
        System.out.println("sequence = " + sequence);
    }
}
