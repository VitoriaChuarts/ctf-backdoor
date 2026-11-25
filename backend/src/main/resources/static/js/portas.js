import { api } from "./api.js";
import { el, limpar } from "./dom.js";

const NIVEIS = {
  EASY: ["facil", "fácil de abrir"],
  MEDIUM: ["media", "dá trabalho"],
  HARD: ["dificil", "bem trancada"],
};

const porId = (id) => document.getElementById(id);

export function montarPortas() {
  const lista = porId("portas");
  const janela = porId("janela-porta");
  const selo = porId("porta-selo");
  const titulo = porId("porta-titulo");
  const descricao = porId("porta-descricao");
  const ambiente = porId("porta-ambiente");
  const btnLigar = porId("btn-ligar");
  const link = porId("porta-link");
  const expira = porId("porta-expira");
  const form = porId("form-flag");
  const campo = porId("campo-flag");
  const btnGirar = porId("btn-girar");
  const msg = porId("porta-msg");

  let abertas = new Set();
  let atual = null;
  let recem = null;

  function mostrar(texto, tipo) {
    msg.textContent = texto;
    msg.className = "msg " + tipo;
  }

  function criarPorta(d) {
    const [classe, rotulo] = NIVEIS[d.difficulty] ?? NIVEIS.EASY;
    const aberta = abertas.has(d.id) ? " aberta" : "";
    const nova = recem === d.id ? " recem" : "";
    return el(
      "button",
      { class: `porta ${classe}${aberta}${nova}`, type: "button", onclick: () => abrirJanela(d) },
      el("span", { class: "placa", texto: "PORTA " + d.id }),
      el("span", { class: "nome", texto: d.title }),
      el("span", { class: "categoria", texto: `${d.category} · ${rotulo}` }),
      el("span", { class: "valor", texto: d.points + " chaves" })
    );
  }

  function desenhar(desafios) {
    limpar(lista);
    if (desafios.length === 0) {
      lista.append(el("p", { class: "vazio", texto: "Nenhuma porta por aqui ainda. Volte logo!" }));
      return;
    }
    for (const d of desafios) {
      lista.append(criarPorta(d));
    }
    recem = null;
  }

  async function recarregar() {
    const [todas, progresso] = await Promise.all([api.portas(), api.progresso()]);
    if (!todas.ok) {
      limpar(lista);
      lista.append(el("p", { class: "vazio", texto: "Não consegui carregar as portas. Tenta recarregar a página." }));
      return;
    }
    abertas = new Set(progresso.ok ? progresso.dados.solved : []);
    desenhar(todas.dados);
  }

  function abrirJanela(d) {
    atual = d;
    const jaAberta = abertas.has(d.id);
    selo.textContent = `${d.category} · ${d.points} chaves`;
    titulo.textContent = d.title;
    descricao.textContent = d.description;
    ambiente.hidden = !d.hasContainer;
    link.hidden = true;
    expira.textContent = "";
    campo.value = "";
    form.hidden = jaAberta;
    if (jaAberta) {
      mostrar("Você já abriu esta porta. Escolhe outra!", "ok");
    } else {
      mostrar("", "");
    }
    janela.showModal();
  }

  btnLigar.addEventListener("click", async () => {
    btnLigar.disabled = true;
    mostrar("Montando o quartinho do desafio...", "");
    const r = await api.ligarAmbiente(atual.id);
    btnLigar.disabled = false;

    if (!r.ok) {
      mostrar(r.dados?.error ?? "Não consegui ligar o ambiente.", "erro");
      return;
    }
    const url = String(r.dados.url);
    if (!url.startsWith("http://") && !url.startsWith("https://")) {
      mostrar("Recebi um endereço estranho. Melhor não abrir.", "erro");
      return;
    }
    link.href = url;
    link.textContent = url;
    link.hidden = false;
    const hora = new Date(r.dados.expiresAt).toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" });
    expira.textContent = "Esse ambiente some sozinho às " + hora + ".";
    mostrar("Ambiente pronto! A flag está escondida lá dentro.", "ok");
  });

  form.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    const flag = campo.value.trim();
    if (flag === "") {
      return;
    }
    btnGirar.disabled = true;
    const r = await api.enviarFlag(atual.id, flag);
    btnGirar.disabled = false;

    if (r.status === 429) {
      mostrar("Calma! Tentativas demais. Respira 1 minuto e volta.", "erro");
      return;
    }
    if (!r.ok) {
      mostrar(r.dados?.error ?? "Algo deu errado. Tenta de novo.", "erro");
      return;
    }
    if (r.dados.correct) {
      abertas.add(atual.id);
      recem = atual.id;
      form.hidden = true;
      mostrar(`Clic! Porta aberta! +${r.dados.points} chaves`, "ok");
      recarregar();
    } else {
      mostrar("Nada... a chave não encaixou.", "erro");
    }
  });

  porId("fechar-porta").addEventListener("click", () => janela.close());

  return { recarregar };
}
