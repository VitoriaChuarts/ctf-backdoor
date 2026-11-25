import { api, lerToken, limparToken, quandoSessaoExpirar } from "./api.js";
import { el, limpar } from "./dom.js";
import { iniciarMascote } from "./mascote.js";
import { montarConta } from "./conta.js";
import { montarPortas } from "./portas.js";
import { montarPlacar } from "./placar.js";

const areaUsuario = document.getElementById("area-usuario");
const boasVindas = document.getElementById("boas-vindas");
const jogo = document.getElementById("jogo");

let usuario = null;

const conta = montarConta({ aoEntrar: entrar });
const portas = montarPortas();
const placar = montarPlacar({ quemSou: () => usuario });

function desenharTopo() {
  limpar(areaUsuario);
  if (usuario === null) {
    areaUsuario.append(
      el("button", { class: "btn", type: "button", texto: "Entrar", onclick: () => conta.abrir("entrar") })
    );
    return;
  }
  areaUsuario.append(
    el("span", { class: "etiqueta-usuario", texto: usuario }),
    el("button", { class: "btn btn-fantasma", type: "button", texto: "Sair", onclick: sair })
  );
}

function entrar(nome) {
  usuario = nome;
  boasVindas.hidden = true;
  jogo.hidden = false;
  desenharTopo();
  portas.recarregar();
  placar.iniciar();
}

function sair() {
  limparToken();
  usuario = null;
  placar.parar();
  jogo.hidden = true;
  boasVindas.hidden = false;
  desenharTopo();
}

quandoSessaoExpirar(sair);

document.getElementById("btn-criar").addEventListener("click", () => conta.abrir("criar"));
document.getElementById("btn-entrar-hero").addEventListener("click", () => conta.abrir("entrar"));

async function iniciar() {
  iniciarMascote();
  desenharTopo();
  if (lerToken()) {
    const r = await api.eu();
    if (r.ok) {
      entrar(r.dados.username);
    } else {
      limparToken();
    }
  }
}

iniciar();
