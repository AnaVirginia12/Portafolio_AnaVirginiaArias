package TechShop.Virginia.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;

@Entity
@Table(name = "categoria")
public class Categoria implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Integer idCategoria;

    @Column(unique = true, nullable = false, length = 50)
    @NotNull
    @Size(max = 50)
    private String descripcion;

    @Column(name = "ruta_imagen", length = 1024)
    @Size(max = 1024)
    private String rutaImagen;

    @Column(name = "activo")
    private Boolean activo;

    // Getters
    public Integer getIdCategoria() { return idCategoria; }
    public String getDescripcion() { return descripcion; }
    public String getRutaImagen() { return rutaImagen; }
    public Boolean getActivo() { return activo; }

    // Setters
    public void setIdCategoria(Integer idCategoria) { this.idCategoria = idCategoria; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setRutaImagen(String rutaImagen) { this.rutaImagen = rutaImagen; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}