package com.vitoria.ctf.challenge;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FlagHasherTest {

    @Test
    void hashDeUmTextoConhecido() {
        assertThat(FlagHasher.sha256("abc"))
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }

    @Test
    void ignoraEspacosNasPontas() {
        assertThat(FlagHasher.matches("  CTF{a}  ", FlagHasher.sha256("CTF{a}"))).isTrue();
    }

    @Test
    void flagErradaNaoBate() {
        assertThat(FlagHasher.matches("CTF{b}", FlagHasher.sha256("CTF{a}"))).isFalse();
    }
}
