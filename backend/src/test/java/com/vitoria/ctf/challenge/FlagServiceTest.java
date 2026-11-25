package com.vitoria.ctf.challenge;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FlagServiceTest {

    private final FlagService flags = new FlagService("segredo-de-teste-bem-comprido-123456");

    @Test
    void cadaJogadorTemUmaFlagDiferente() {
        assertThat(flags.flagFor(1, 10)).isNotEqualTo(flags.flagFor(2, 10));
    }

    @Test
    void amesmaCombinacaoGeraSempreAMesmaFlag() {
        assertThat(flags.flagFor(1, 10)).isEqualTo(flags.flagFor(1, 10));
    }

    @Test
    void descobreODonoDaFlag() {
        assertThat(flags.ownerOf(flags.flagFor(7, 10), 10)).contains(7L);
    }

    @Test
    void flagDeOutroDesafioNaoServe() {
        assertThat(flags.ownerOf(flags.flagFor(1, 10), 11)).isEmpty();
    }

    @Test
    void flagAdulteradaEhRecusada() {
        String falsa = flags.flagFor(1, 10).replace("u1_", "u2_");
        assertThat(flags.ownerOf(falsa, 10)).isEmpty();
    }

    @Test
    void textoQualquerNaoQuebraNada() {
        assertThat(flags.ownerOf("CTF{qualquer-coisa}", 10)).isEmpty();
        assertThat(flags.ownerOf("", 10)).isEmpty();
    }
}
