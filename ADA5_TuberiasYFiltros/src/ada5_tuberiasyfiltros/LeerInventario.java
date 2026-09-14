/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ada5_tuberiasyfiltros;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author ojeda
 */
public class LeerInventario {

    public List<Producto> leerArchivo() throws FileNotFoundException {

    File inventarioArchivo = new File("ADA5_TuberiasYFiltros\\Inventario.txt");
    List<Producto> inventario = new ArrayList<>();

    Scanner scanner = new Scanner(inventarioArchivo);

    while (scanner.hasNextLine()) {

        String linea = scanner.nextLine().trim();
        String[] datos = linea.split(",");

        String articulo = datos[0].replace("\uFEFF", "").trim();
        int existencia = Integer.parseInt(datos[1].trim());
        double precio = Double.parseDouble(datos[2].trim());

        Producto producto = new Producto(articulo, existencia, precio);

        inventario.add(producto);
    }

    scanner.close();

    return inventario;
}
}
