export function iniciarMascote() {
  const esquerda = document.getElementById("pupila-e");
  const direita = document.getElementById("pupila-d");
  const corpo = document.getElementById("portinha");
  if (!esquerda || !direita || !corpo) {
    return;
  }

  const ALCANCE = 5;

  document.addEventListener("pointermove", (evento) => {
    const caixa = corpo.getBoundingClientRect();
    const centroX = caixa.left + caixa.width / 2;
    const centroY = caixa.top + caixa.height * 0.3;

    const dx = evento.clientX - centroX;
    const dy = evento.clientY - centroY;
    const distancia = Math.hypot(dx, dy) || 1;
    const forca = Math.min(distancia, 140) / 140;

    const x = (dx / distancia) * ALCANCE * forca;
    const y = (dy / distancia) * ALCANCE * forca;

    for (const pupila of [esquerda, direita]) {
      pupila.style.transform = `translate(${x}px, ${y}px)`;
    }
  });
}
