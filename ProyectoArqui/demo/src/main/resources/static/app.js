/* JS movido del index.html para mantenerlo limpio */
const URL_API = "/api/empleados";
const FILAS_POR_PAGINA = 6;

let empleados = [];
let seleccionados = new Set();
let paginaActual = 1;
let idEnEdicion = null;

const cuerpoTabla = document.getElementById("cuerpoTabla");
const formulario = document.getElementById("formulario");
const mensaje = document.getElementById("mensaje");

async function llamarApi(ruta, opciones) {
    const respuesta = await fetch(URL_API + ruta, opciones);
    if (!respuesta.ok) {
        let texto = "Error " + respuesta.status;
        try {
            const cuerpo = await respuesta.json();
            if (cuerpo && cuerpo.error) texto = cuerpo.error;
        } catch (e) { /* sin cuerpo JSON */ }
        throw new Error(texto);
    }
    if (respuesta.status === 204) return null;
    return respuesta.json();
}

async function cargarEmpleados() {
    try {
        empleados = await llamarApi("");
        const ids = new Set(empleados.map(e => e.idEmpleado));
        seleccionados = new Set([...seleccionados].filter(id => ids.has(id)));
        mostrarMensaje("");
    } catch (e) {
        empleados = [];
        mostrarMensaje("No se pudo cargar la lista: " + e.message);
    }
    dibujar();
}

function totalPaginas() {
    return Math.max(1, Math.ceil(empleados.length / FILAS_POR_PAGINA));
}

function dibujar() {
    if (paginaActual > totalPaginas()) paginaActual = totalPaginas();
    if (paginaActual < 1) paginaActual = 1;

    const inicio = (paginaActual - 1) * FILAS_POR_PAGINA;
    const pagina = empleados.slice(inicio, inicio + FILAS_POR_PAGINA);

    cuerpoTabla.replaceChildren();

    if (pagina.length === 0) {
        const fila = document.createElement("tr");
        const celda = document.createElement("td");
        celda.colSpan = 6;
        celda.textContent = "No hay empleados registrados.";
        celda.style.textAlign = "center";
        fila.appendChild(celda);
        cuerpoTabla.appendChild(fila);
    }

    for (const empleado of pagina) {
        cuerpoTabla.appendChild(crearFila(empleado));
    }

    actualizarSeleccionarTodos(pagina);
    dibujarPaginacion();
}

function crearFila(empleado) {
    const fila = document.createElement("tr");

    // Checkbox
    const celdaCheck = document.createElement("td");
    const check = document.createElement("input");
    check.type = "checkbox";
    check.checked = seleccionados.has(empleado.idEmpleado);
    check.addEventListener("change", () => {
        if (check.checked) seleccionados.add(empleado.idEmpleado);
        else seleccionados.delete(empleado.idEmpleado);
        actualizarSeleccionarTodos(paginaVisible());
    });
    celdaCheck.appendChild(check);
    fila.appendChild(celdaCheck);

    // Datos del empleado
    for (const valor of [empleado.idEmpleado, empleado.nombre, empleado.edad, empleado.salario]) {
        const celda = document.createElement("td");
        celda.textContent = valor;
        fila.appendChild(celda);
    }

    // Acciones (Iconos SVG)
    const celdaAcciones = document.createElement("td");
    
    // Icono Editar (Lápiz amarillo)
    const btnEditar = document.createElement("button");
    btnEditar.innerHTML = `<svg class="action-icon icon-edit" viewBox="0 0 24 24" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"></path><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"></path></svg>`;
    btnEditar.style.background = 'none';
    btnEditar.style.border = 'none';
    btnEditar.style.padding = '0';
    btnEditar.addEventListener("click", () => abrirFormulario(empleado));

    // Icono Eliminar (Basura roja)
    const btnEliminar = document.createElement("button");
    btnEliminar.innerHTML = `<svg class="action-icon icon-delete" viewBox="0 0 24 24"><path d="M3 6h18M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2m3 0v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6h14zM10 11v6M14 11v6"/></svg>`;
    btnEliminar.style.background = 'none';
    btnEliminar.style.border = 'none';
    btnEliminar.style.padding = '0';
    btnEliminar.addEventListener("click", () => eliminarUno(empleado));

    celdaAcciones.append(btnEditar, btnEliminar);
    fila.appendChild(celdaAcciones);

    return fila;
}

function paginaVisible() {
    const inicio = (paginaActual - 1) * FILAS_POR_PAGINA;
    return empleados.slice(inicio, inicio + FILAS_POR_PAGINA);
}

function actualizarSeleccionarTodos(pagina) {
    const check = document.getElementById("seleccionarTodos");
    check.checked = pagina.length > 0 && pagina.every(e => seleccionados.has(e.idEmpleado));
}

function dibujarPaginacion() {
    const total = totalPaginas();
    const contenedor = document.getElementById("numerosPagina");
    contenedor.replaceChildren();

    for (let n = 1; n <= total; n++) {
        const boton = document.createElement("button");
        boton.type = "button";
        boton.textContent = n;
        if (n === paginaActual) boton.disabled = true;
        boton.addEventListener("click", () => irAPagina(n));
        contenedor.append(boton, " ");
    }

    document.getElementById("btnAnterior").disabled = paginaActual <= 1;
    document.getElementById("btnSiguiente").disabled = paginaActual >= total;
    document.getElementById("infoPagina").textContent =
        "Página " + paginaActual + " de " + total + " (" + empleados.length + " empleados)";
}

function irAPagina(n) {
    paginaActual = n;
    dibujar();
}

function abrirFormulario(empleado) {
    formulario.reset();
    if (empleado) {
        idEnEdicion = empleado.idEmpleado;
        document.getElementById("tituloFormulario").textContent = "Editar empleado (ID " + empleado.idEmpleado + ")";
        document.getElementById("nombre").value = empleado.nombre;
        document.getElementById("edad").value = empleado.edad;
        document.getElementById("salario").value = empleado.salario;
    } else {
        idEnEdicion = null;
        document.getElementById("tituloFormulario").textContent = "Agregar empleado";
    }
    formulario.hidden = false;
    document.getElementById("nombre").focus();
}

function cerrarFormulario() {
    formulario.hidden = true;
    formulario.reset();
    idEnEdicion = null;
}

formulario.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    const datos = {
        nombre: document.getElementById("nombre").value.trim(),
        edad: Number(document.getElementById("edad").value),
        salario: Number(document.getElementById("salario").value)
    };
    const editando = idEnEdicion !== null;

    try {
        await llamarApi(editando ? "/" + idEnEdicion : "", {
            method: editando ? "PUT" : "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(datos)
        });
        cerrarFormulario();
        await cargarEmpleados();
        if (!editando) paginaActual = totalPaginas();
        dibujar();
        mostrarMensaje(editando ? "Empleado actualizado." : "Empleado creado.");
    } catch (e) {
        mostrarMensaje("No se pudo guardar: " + e.message);
    }
});

async function eliminarUno(empleado) {
    if (!confirm("¿Eliminar a " + empleado.nombre + "?")) return;
    try {
        await llamarApi("/" + empleado.idEmpleado, { method: "DELETE" });
        seleccionados.delete(empleado.idEmpleado);
        await cargarEmpleados();
        mostrarMensaje("Empleado eliminado.");
    } catch (e) {
        mostrarMensaje("No se pudo eliminar: " + e.message);
    }
}

async function eliminarSeleccionados() {
    if (seleccionados.size === 0) {
        mostrarMensaje("Selecciona al menos un empleado para eliminar.");
        return;
    }
    if (!confirm("¿Eliminar los " + seleccionados.size + " empleados seleccionados?")) return;
    try {
        await llamarApi("?ids=" + [...seleccionados].join(","), { method: "DELETE" });
        seleccionados.clear();
        await cargarEmpleados();
        mostrarMensaje("Empleados eliminados.");
    } catch (e) {
        mostrarMensaje("No se pudieron eliminar: " + e.message);
    }
}

function mostrarMensaje(texto) {
    mensaje.textContent = texto;
}

document.getElementById("btnNuevo").addEventListener("click", () => abrirFormulario(null));
document.getElementById("btnCancelar").addEventListener("click", cerrarFormulario);
document.getElementById("btnEliminarSeleccionados").addEventListener("click", eliminarSeleccionados);
document.getElementById("btnAnterior").addEventListener("click", () => irAPagina(paginaActual - 1));
document.getElementById("btnSiguiente").addEventListener("click", () => irAPagina(paginaActual + 1));
document.getElementById("seleccionarTodos").addEventListener("change", (evento) => {
    for (const e of paginaVisible()) {
        if (evento.target.checked) seleccionados.add(e.idEmpleado);
        else seleccionados.delete(e.idEmpleado);
    }
    dibujar();
});

cargarEmpleados();