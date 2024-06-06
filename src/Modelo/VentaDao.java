
package Modelo;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.filechooser.FileSystemView;

public class VentaDao {
    Connection con;
    Conexion cn = new Conexion();
    PreparedStatement ps;
    ResultSet rs;
    int r;
    
    public int IdVenta(){
        int id = 0;
        String sql = "SELECT MAX(id) FROM ventas";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                id = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return id;
    }
    
    public int RegistrarVenta(Venta v){
        String sql = "INSERT INTO ventas (cliente, vendedor, total, fecha) VALUES (?,?,?,?)";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, v.getCliente());
            ps.setString(2, v.getVendedor());
            ps.setDouble(3, v.getTotal());
            ps.setString(4, v.getFecha());
            ps.execute();
        } catch (SQLException e) {
            System.out.println(e.toString());
        }finally{
            try {
                con.close();
            } catch (SQLException e) {
                System.out.println(e.toString());
            }
        }
        return r;
    }
    
    public int RegistrarDetalle(Detalle Dv){
       String sql = "INSERT INTO detalle (id_pro, cantidad, precio, id_venta) VALUES (?,?,?,?)";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, Dv.getId_pro());
            ps.setInt(2, Dv.getCantidad());
            ps.setDouble(3, Dv.getPrecio());
            ps.setInt(4, Dv.getId());
            ps.execute();
        } catch (SQLException e) {
            System.out.println(e.toString());
        }finally{
            try {
                con.close();
            } catch (SQLException e) {
                System.out.println(e.toString());
            }
        }
        return r;
    }
    
    public boolean ActualizarStock(int cant, int id){
        String sql = "UPDATE productos SET stock = ? WHERE id = ?";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1,cant);
            ps.setInt(2, id);
            ps.execute();
            return true;
        } catch (SQLException e) {
            System.out.println(e.toString());
            return false;
        }
    }
    
    public List Listarventas(){
       List<Venta> ListaVenta = new ArrayList();
       String sql = "SELECT c.id AS id_cli, c.nombre, v.* FROM clientes c INNER JOIN ventas v ON c.id = v.cliente";
       try {
           con = cn.getConnection();
           ps = con.prepareStatement(sql);
           rs = ps.executeQuery();
           while (rs.next()) {               
               Venta vent = new Venta();
               vent.setId(rs.getInt("id"));
               vent.setNombre_cli(rs.getString("nombre"));
               vent.setVendedor(rs.getString("vendedor"));
               vent.setTotal(rs.getDouble("total"));
               ListaVenta.add(vent);
           }
       } catch (SQLException e) {
           System.out.println(e.toString());
       }
       return ListaVenta;
   }
    public Venta BuscarVenta(int id){
        Venta cl = new Venta();
        String sql = "SELECT * FROM ventas WHERE id = ?";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                cl.setId(rs.getInt("id"));
                cl.setCliente(rs.getInt("cliente"));
                cl.setTotal(rs.getDouble("total"));
                cl.setVendedor(rs.getString("vendedor"));
                cl.setFecha(rs.getString("fecha"));
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return cl;
    }
    public void pdfV(int idVenta, int idCliente, double total, String usuario) {
    try {
        Date fechaActual = new Date();
        FileOutputStream archivo;
        String url = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
        File salida = new File(url + File.separator + "venta.pdf");
        archivo = new FileOutputStream(salida);
        Document documento = new Document();
        PdfWriter.getInstance(documento, archivo);
        documento.open();
        
        // Logo
        Image logo = Image.getInstance(getClass().getResource("/Img/logo.jpg"));
        logo.setAlignment(Element.ALIGN_CENTER);
        documento.add(logo);
        
        // Encabezado
        Paragraph encabezado = new Paragraph();
        encabezado.setAlignment(Element.ALIGN_CENTER);
        encabezado.add("\n\nVendedor: " + usuario + "\nFolio: " + idVenta + "\nFecha: " +
                new SimpleDateFormat("dd/MM/yyyy").format(fechaActual) + "\n\n");
        documento.add(encabezado);
        
        // Información de la empresa
        PdfPTable infoEmpresa = new PdfPTable(1);
        infoEmpresa.setWidthPercentage(100);
        PdfPCell celdaInfoEmpresa = new PdfPCell();
        String consultaEmpresa = "SELECT * FROM config";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(consultaEmpresa);
            rs = ps.executeQuery();
            if (rs.next()) {
                Font fontAzul = new Font(Font.FontFamily.UNDEFINED, 12, Font.NORMAL, BaseColor.BLUE);
                //String mensaje = rs.getString("mensaje");
                celdaInfoEmpresa.addElement(new Paragraph("RUC: " + rs.getString("ruc"), fontAzul));
                celdaInfoEmpresa.addElement(new Paragraph("Nombre: " + rs.getString("nombre"), fontAzul));
                celdaInfoEmpresa.addElement(new Paragraph("Teléfono: " + rs.getString("telefono"), fontAzul));
                celdaInfoEmpresa.addElement(new Paragraph("Dirección: " + rs.getString("direccion") + "\n\n", fontAzul));
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        celdaInfoEmpresa.setBorder(Rectangle.NO_BORDER);
        infoEmpresa.addCell(celdaInfoEmpresa);
        documento.add(infoEmpresa);
        
        // Datos del cliente
        // Datos del cliente
Paragraph datosCliente = new Paragraph("DATOS DEL CLIENTE\n\n");
documento.add(datosCliente);

PdfPTable tablaCliente = new PdfPTable(3);
tablaCliente.setWidthPercentage(100);
tablaCliente.setHorizontalAlignment(Element.ALIGN_CENTER);

// Definir el color azul para los datos del cliente
Font fontAzul = new Font(Font.FontFamily.UNDEFINED, 12, Font.NORMAL, BaseColor.BLUE);

PdfPCell celdaNombre = new PdfPCell(new Phrase("Nombre", fontAzul)); // Aplicar color azul al nombre del cliente
PdfPCell celdaTelefono = new PdfPCell(new Phrase("Teléfono", fontAzul)); // Aplicar color azul al teléfono del cliente
PdfPCell celdaDireccion = new PdfPCell(new Phrase("Dirección", fontAzul)); // Aplicar color azul a la dirección del cliente

tablaCliente.addCell(celdaNombre);
tablaCliente.addCell(celdaTelefono);
tablaCliente.addCell(celdaDireccion);

String consultaCliente = "SELECT * FROM clientes WHERE id = ?";
try {
    ps = con.prepareStatement(consultaCliente);
    ps.setInt(1, idCliente);
    rs = ps.executeQuery();
    if (rs.next()) {
        tablaCliente.addCell(new Phrase(rs.getString("nombre"))); 
        tablaCliente.addCell(new Phrase(rs.getString("telefono"))); 
        tablaCliente.addCell(new Phrase(rs.getString("direccion") + "\n\n")); 
    } else {
        tablaCliente.addCell("Público en General");
        tablaCliente.addCell("S/N");
        tablaCliente.addCell("S/N" + "\n\n");
    }
} catch (SQLException e) {
    System.out.println(e.toString());
}

documento.add(tablaCliente);
        
        // Detalles de la venta
        PdfPTable tablaDetalles = new PdfPTable(4);
        tablaDetalles.setWidthPercentage(100);
        tablaDetalles.setHorizontalAlignment(Element.ALIGN_CENTER);
        PdfPCell celdaCantidad = new PdfPCell(new Phrase("Cantidad"));
        PdfPCell celdaDescripcion = new PdfPCell(new Phrase("Descripción"));
        PdfPCell celdaPrecioUnitario = new PdfPCell(new Phrase("P. unit."));
        PdfPCell celdaPrecioTotal = new PdfPCell(new Phrase("P. Total"));
        tablaDetalles.addCell(celdaCantidad);
        tablaDetalles.addCell(celdaDescripcion);
        tablaDetalles.addCell(celdaPrecioUnitario);
        tablaDetalles.addCell(celdaPrecioTotal);
        String consultaProductos = "SELECT d.id, d.id_pro, d.id_venta, d.precio, d.cantidad, p.id, p.nombre FROM detalle d INNER JOIN productos p ON d.id_pro = p.id WHERE d.id_venta = ?";
        try {
            ps = con.prepareStatement(consultaProductos);
            ps.setInt(1, idVenta);
            rs = ps.executeQuery();
            while (rs.next()) {
                double subTotal = rs.getInt("cantidad") * rs.getDouble("precio");
                tablaDetalles.addCell(rs.getString("cantidad"));
                tablaDetalles.addCell(rs.getString("nombre"));
                tablaDetalles.addCell(rs.getString("precio"));
                tablaDetalles.addCell(String.valueOf(subTotal));
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        documento.add(tablaDetalles);
        
        // Total
        Paragraph totalVenta = new Paragraph("\nTotal: $" + total);
        totalVenta.setAlignment(Element.ALIGN_RIGHT);
        documento.add(totalVenta);
        
        // Firma
        Paragraph firma = new Paragraph("\nCancelación\n\n------------------------------------\nFirma\n\n");
        firma.setAlignment(Element.ALIGN_CENTER);
        documento.add(firma);
        
        // Mensaje de la empresa
Paragraph mensajeEmpresa = new Paragraph();
mensajeEmpresa.setAlignment(Element.ALIGN_CENTER);
String mensaje = "";
String consultaEmpres = "SELECT mensaje FROM config"; // Solo necesitamos el mensaje de la consulta
try {
    ps = con.prepareStatement(consultaEmpresa);
    rs = ps.executeQuery();
    if (rs.next()) {
        mensaje = rs.getString("mensaje");
    }
} catch (SQLException e) {
    System.out.println(e.toString());
}
mensajeEmpresa.add(mensaje); // Agregamos el mensaje al párrafo
documento.add(mensajeEmpresa); // Agregamos el párrafo al documento
        
        documento.close();
        archivo.close();
        Desktop.getDesktop().open(salida);
    } catch (DocumentException | IOException e) {
        System.out.println(e.toString());
    }
}

    public boolean EliminarVenta(int id){
       String sql = "DELETE FROM ventas WHERE id = ?";
       try {
           ps = con.prepareStatement(sql);
           ps.setInt(1, id);
           ps.execute();
           return true;
       } catch (SQLException e) {
           System.out.println(e.toString());
           return false;
       }finally{
           try {
               con.close();
           } catch (SQLException ex) {
               System.out.println(ex.toString());
           }
       }
   }

    
}
