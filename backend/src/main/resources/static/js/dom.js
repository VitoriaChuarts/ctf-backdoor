// Ajudantes para criar e limpar elementos da página sem innerHTML.

export function el(tag, attrs = {}, ...filhos) {
  const no = document.createElement(tag);

  const ehAtributos =
    attrs !== null &&
    typeof attrs === "object" &&
    !(attrs instanceof Node) &&
    !Array.isArray(attrs);

  if (ehAtributos) {
    for (const [chave, valor] of Object.entries(attrs)) {
      if (valor === null || valor === undefined || valor === false) continue;
      if (chave === "class" || chave === "classe" || chave === "className") {
        no.className = valor;
      } else if (chave === "texto" || chave === "text") {
        no.textContent = valor;
      } else if (chave.startsWith("on") && typeof valor === "function") {
        no.addEventListener(chave.slice(2).toLowerCase(), valor);
      } else {
        no.setAttribute(chave, valor === true ? "" : valor);
      }
    }
  } else {
    filhos.unshift(attrs);
  }

  adicionar(no, filhos);
  return no;
}

function adicionar(pai, filhos) {
  for (const filho of filhos) {
    if (filho === null || filho === undefined || filho === false) continue;
    if (Array.isArray(filho)) {
      adicionar(pai, filho);
    } else if (filho instanceof Node) {
      pai.appendChild(filho);
    } else {
      pai.appendChild(document.createTextNode(String(filho)));
    }
  }
}

export function limpar(no) {
  no.replaceChildren();
  return no;
}
