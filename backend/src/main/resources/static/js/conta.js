import { api } from "./api.js";

function erroDeCadastro(r) {
  if (r.status === 409) {
    return "Esse apelido ou e-mail já tem dono. Tenta outro!";
  }
  if (r.status === 400) {
    return "Confere os campos: apelido de 3 a 30 letras, números ou _, e-mail válido e senha com 8+ caracteres.";
  }
  return "Algo deu errado do nosso lado. Tenta de novo daqui a pouco.";
}

function erroDeLogin(r) {
  if (r.status === 401) {
    return "Apelido ou senha não bateram.";
  }
  if (r.status === 429) {
    return "Calma! Muitas tentativas. Respira uns minutinhos e volta.";
  }
  return "Algo deu errado do nosso lado. Tenta de novo daqui a pouco.";
}

export function montarConta({ aoEntrar }) {
  const janela = document.getElementById("janela-conta");
  const form = document.getElementById("form-conta");
  const msg = document.getElementById("msg-conta");
  const linhaEmail = document.getElementById("linha-email");
  const abaEntrar = document.getElementById("aba-entrar");
  const abaCriar = document.getElementById("aba-criar");
  const botao = document.getElementById("btn-conta");
  let modo = "entrar";

  function mostrar(texto, tipo) {
    msg.textContent = texto;
    msg.className = "msg " + tipo;
  }

  function definirModo(novo) {
    modo = novo;
    linhaEmail.hidden = modo === "entrar";
    abaEntrar.setAttribute("aria-pressed", String(modo === "entrar"));
    abaCriar.setAttribute("aria-pressed", String(modo === "criar"));
    botao.textContent = modo === "entrar" ? "Abrir a porta" : "Criar minha conta";
    form.elements.senha.autocomplete = modo === "entrar" ? "current-password" : "new-password";
    mostrar("", "");
  }

  abaEntrar.addEventListener("click", () => definirModo("entrar"));
  abaCriar.addEventListener("click", () => definirModo("criar"));
  document.getElementById("fechar-conta").addEventListener("click", () => janela.close());

  form.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    const usuario = form.elements.usuario.value.trim();
    const senha = form.elements.senha.value;
    botao.disabled = true;

    try {
      if (modo === "criar") {
        const email = form.elements.email.value.trim();
        const cadastro = await api.cadastrar(usuario, email, senha);
        if (!cadastro.ok) {
          mostrar(erroDeCadastro(cadastro), "erro");
          return;
        }
      }
      const login = await api.entrar(usuario, senha);
      if (!login.ok) {
        mostrar(erroDeLogin(login), "erro");
        return;
      }
      form.reset();
      janela.close();
      aoEntrar(usuario);
    } finally {
      botao.disabled = false;
    }
  });

  return {
    abrir(m) {
      definirModo(m);
      janela.showModal();
    },
  };
}
