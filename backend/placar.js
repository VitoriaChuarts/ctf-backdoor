import { api } from "./api.js";
import { el, limpar } from "./dom.js";

export function montarPlacar({ quemSou }) {
  const lista = document.getElementById("placar");
  let fonte = null;

  function desenhar(entradas) {
    limpar(lista);
    if (entradas.length === 0) {
      lista.append(el("li", { class: "vazio", texto: "Ninguém abriu nada ainda. Seja a primeira!" }));
      return;
    }
    for (const e of entradas) {
      const sou = e.username === quemSou();
      lista.append(
        el("li", { class: sou ? "voce" : "" },
          el("span", { class: "pos", texto: String(e.rank) }),
          el("span", { class: "nome", texto: sou ? e.username + " (você)" : e.username }),
          el("span", { class: "pts", texto: String(e.score) })
        )
      );
    }
  }

  function parar() {
    if (fonte !== null) {
      fonte.close();
      fonte = null;
    }
  }

  async function iniciar() {
    parar();
    const inicial = await api.placar();
    if (inicial.ok) {
      desenhar(inicial.dados);
    }
    fonte = new EventSource("/api/scoreboard/stream");
    fonte.addEventListener("scoreboard", (evento) => {
      desenhar(JSON.parse(evento.data));
    });
  }

  return { iniciar, parar };
}
