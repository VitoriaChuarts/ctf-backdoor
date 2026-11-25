const CHAVE_TOKEN = "ctf-backdoor:token";

let aoExpirar = () => {};

export function quandoSessaoExpirar(funcao) {
  aoExpirar = funcao;
}

export function lerToken() {
  return localStorage.getItem(CHAVE_TOKEN);
}

function guardarToken(token) {
  localStorage.setItem(CHAVE_TOKEN, token);
}

export function limparToken() {
  localStorage.removeItem(CHAVE_TOKEN);
}

async function chamar(caminho, { metodo = "GET", corpo, auth = true } = {}) {
  const cabecalhos = {};
  if (corpo !== undefined) {
    cabecalhos["Content-Type"] = "application/json";
  }
  const token = lerToken();
  if (auth && token) {
    cabecalhos["Authorization"] = "Bearer " + token;
  }

  const resposta = await fetch(caminho, {
    method: metodo,
    headers: cabecalhos,
    body: corpo !== undefined ? JSON.stringify(corpo) : undefined,
  });

  let dados = null;
  try {
    dados = await resposta.json();
  } catch {
    // resposta sem corpo JSON: tudo bem
  }

  if (resposta.status === 401 && auth) {
    limparToken();
    aoExpirar();
  }
  return { ok: resposta.ok, status: resposta.status, dados };
}

export const api = {
  cadastrar: (username, email, password) =>
    chamar("/api/auth/register", { metodo: "POST", corpo: { username, email, password }, auth: false }),

  entrar: async (username, password) => {
    const r = await chamar("/api/auth/login", { metodo: "POST", corpo: { username, password }, auth: false });
    if (r.ok) {
      guardarToken(r.dados.token);
    }
    return r;
  },

  eu: () => chamar("/api/me"),
  progresso: () => chamar("/api/me/progress"),
  portas: () => chamar("/api/challenges"),
  enviarFlag: (id, flag) => chamar(`/api/challenges/${id}/submit`, { metodo: "POST", corpo: { flag } }),
  ligarAmbiente: (id) => chamar(`/api/challenges/${id}/instance`, { metodo: "POST" }),
  placar: () => chamar("/api/scoreboard", { auth: false }),
};
