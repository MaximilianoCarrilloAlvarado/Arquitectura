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
        } catch (e) { /* Respuesta sin cuerpo JSON. */ }
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
        dibujar();
        return true;
    } catch (e) {
        mostrarMensaje("No se pudo cargar la lista: " + e.message, true);
        return false;
    }
}

function totalPaginas() {
    return Math.max(1, Math.ceil(empleados.length / FILAS_POR_PAGINA));
}

function paginaVisible() {
    const inicio = (paginaActual - 1) * FILAS_POR_PAGINA;
    return empleados.slice(inicio, inicio + FILAS_POR_PAGINA);
}

function dibujar() {
    paginaActual = Math.max(1, Math.min(paginaActual, totalPaginas()));
    const pagina = paginaVisible();
    cuerpoTabla.replaceChildren();

    if (pagina.length === 0) {
        const fila = document.createElement("tr");
        const celda = document.createElement("td");
        celda.colSpan = 6;
        celda.textContent = "No hay empleados registrados.";
        fila.appendChild(celda);
        cuerpoTabla.appendChild(fila);
    }

    for (const empleado of pagina) {
        cuerpoTabla.appendChild(crearFila(empleado));
    }

    actualizarSeleccionarTodos(pagina);
    dibujarPaginacion();
    document.getElementById("btnEliminarSeleccionados").disabled = seleccionados.size === 0;
}

function crearFila(empleado) {
    const fila = document.createElement("tr");
    const celdaCheck = document.createElement("td");
    const check = document.createElement("input");
    check.type = "checkbox";
    check.setAttribute("aria-label", "Seleccionar a " + empleado.nombre);
    check.checked = seleccionados.has(empleado.idEmpleado);
    check.addEventListener("change", () => {
        if (check.checked) seleccionados.add(empleado.idEmpleado);
        else seleccionados.delete(empleado.idEmpleado);
        actualizarSeleccionarTodos(paginaVisible());
        document.getElementById("btnEliminarSeleccionados").disabled = seleccionados.size === 0;
    });
    celdaCheck.appendChild(check);
    fila.appendChild(celdaCheck);

    for (const valor of [empleado.idEmpleado, empleado.nombre, empleado.edad, empleado.salario]) {
        const celda = document.createElement("td");
        celda.textContent = valor;
        fila.appendChild(celda);
    }

    const celdaAcciones = document.createElement("td");
    const btnEditar = document.createElement("button");
    btnEditar.type = "button";
    btnEditar.className = "accion-fila";
    btnEditar.textContent = "Editar";
    btnEditar.setAttribute("aria-label", "Editar a " + empleado.nombre);
    btnEditar.addEventListener("click", () => abrirFormulario(empleado));
    const btnEliminar = document.createElement("button");
    btnEliminar.type = "button";
    btnEliminar.className = "accion-fila eliminar";
    btnEliminar.textContent = "Eliminar";
    btnEliminar.setAttribute("aria-label", "Eliminar a " + empleado.nombre);
    btnEliminar.addEventListener("click", () => eliminarUno(empleado));
    celdaAcciones.append(btnEditar, " ", btnEliminar);
    fila.appendChild(celdaAcciones);

    return fila;
}

function actualizarSeleccionarTodos(pagina) {
    const check = document.getElementById("seleccionarTodos");
    const cantidad = pagina.filter(e => seleccionados.has(e.idEmpleado)).length;
    check.checked = pagina.length > 0 && cantidad === pagina.length;
    check.indeterminate = cantidad > 0 && cantidad < pagina.length;
    check.disabled = pagina.length === 0;
}

function dibujarPaginacion() {
    const total = totalPaginas();
    const contenedor = document.getElementById("numerosPagina");
    contenedor.replaceChildren();

    for (let n = 1; n <= total; n++) {
        const boton = document.createElement("button");
        boton.type = "button";
        boton.textContent = n;
        boton.disabled = n === paginaActual;
        boton.setAttribute("aria-label", "Página " + n);
        if (n === paginaActual) boton.setAttribute("aria-current", "page");
        boton.addEventListener("click", () => irAPagina(n));
        contenedor.append(boton);
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
    if (!datos.nombre) {
        mostrarMensaje("Escribe un nombre válido.", true);
        document.getElementById("nombre").focus();
        return;
    }
    const editando = idEnEdicion !== null;
    const guardar = document.getElementById("btnGuardar");
    guardar.disabled = true;

    try {
        await llamarApi(editando ? "/" + idEnEdicion : "", {
            method: editando ? "PUT" : "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(datos)
        });
        cerrarFormulario();
        const actualizado = await cargarEmpleados();
        if (actualizado && !editando) {
            paginaActual = totalPaginas();
            dibujar();
        }
        if (actualizado) mostrarMensaje(editando ? "Empleado actualizado." : "Empleado creado.");
    } catch (e) {
        mostrarMensaje("No se pudo guardar: " + e.message, true);
    } finally {
        guardar.disabled = false;
    }
});

async function eliminarUno(empleado) {
    if (!confirm("¿Eliminar a " + empleado.nombre + "?")) return;
    try {
        await llamarApi("/" + empleado.idEmpleado, { method: "DELETE" });
        seleccionados.delete(empleado.idEmpleado);
        if (await cargarEmpleados()) mostrarMensaje("Empleado eliminado.");
    } catch (e) {
        mostrarMensaje("No se pudo eliminar: " + e.message, true);
    }
}

async function eliminarSeleccionados() {
    if (seleccionados.size === 0) return;
    if (!confirm("¿Eliminar los " + seleccionados.size + " empleados seleccionados?")) return;
    try {
        await llamarApi("?ids=" + [...seleccionados].join(","), { method: "DELETE" });
        seleccionados.clear();
        if (await cargarEmpleados()) mostrarMensaje("Empleados eliminados.");
    } catch (e) {
        mostrarMensaje("No se pudieron eliminar: " + e.message, true);
    }
}

function mostrarMensaje(texto, error = false) {
    mensaje.textContent = texto;
    mensaje.classList.toggle("error", error);
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

dibujar();
cargarEmpleados();
