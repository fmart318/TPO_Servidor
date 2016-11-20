package rmi;

import java.util.ArrayList;
import java.util.List;

import dto.CargaDTO;
import dto.ClienteDTO;
import dto.DireccionDTO;
import dto.EmpresaDTO;
import dto.EnvioDTO;
import dto.FacturaDTO;
import dto.HabilitadoDTO;
import dto.ParticularDTO;
import dto.PedidoDTO;
import dto.PlanDeMantenimientoDTO;
import dto.PrecioVehiculoDTO;
import dto.ProductoDTO;
import dto.ProveedorDTO;
import dto.RemitoDTO;
import dto.RutaDTO;
import dto.SucursalDTO;
import dto.TransporteDTO;
import dto.TrayectoDTO;
import dto.VehiculoDTO;
import dto.ViajeDTO;
import entities.Carga;
import entities.Cliente;
import entities.Direccion;
import entities.Empresa;
import entities.Envio;
import entities.Factura;
import entities.Habilitado;
import entities.Particular;
import entities.Pedido;
import entities.PlanDeMantenimiento;
import entities.PrecioVehiculo;
import entities.Producto;
import entities.Proveedor;
import entities.Remito;
import entities.Ruta;
import entities.Sucursal;
import entities.Transporte;
import entities.Trayecto;
import entities.Vehiculo;
import entities.Viaje;

public class EntityManager {

	public static Carga CargaToEntity(CargaDTO cargaDTO) {
		return new Carga(cargaDTO.getIdCarga(), cargaDTO.getPeso(), cargaDTO.getAncho(), cargaDTO.getAlto(),
				cargaDTO.getProfundidad(), cargaDTO.getVolumen(), cargaDTO.getFragilidad(), cargaDTO.getTratamiento(),
				cargaDTO.getApilable(), cargaDTO.isRefrigerable(), cargaDTO.getCondiciones(), cargaDTO.isDespachado(),
				cargaDTO.getTipoMercaderia());
	}

	public static Cliente ClienteToEntity(ClienteDTO clienteDTO) {
		return new Cliente(clienteDTO.getIdCliente(), clienteDTO.getNombre());
	}

	public static Direccion DireccionToEntity(DireccionDTO direccionDTO) {
		return new Direccion(direccionDTO.getIdDireccion(), direccionDTO.getCalle(), direccionDTO.getNumero(),
				direccionDTO.getPiso(), direccionDTO.getDepartamento(), direccionDTO.getCP());
	}

	public static Empresa EmpresaToEntity(EmpresaDTO empresaDTO) {
		List<Producto> productos = new ArrayList<Producto>();
		for (ProductoDTO producto : empresaDTO.getProductos()) {
			productos.add(ProductoToEntity(producto));
		}
		return new Empresa(empresaDTO.getIdCliente(), empresaDTO.getNombre(), empresaDTO.getCUIT(),
				empresaDTO.getTipo(), empresaDTO.getDetallePoliticas(), productos,
				empresaDTO.getSaldoCuentaCorriente());
	}

	public static Envio EnvioToEntity(EnvioDTO envioDTO) {
		Envio envio = new Envio(envioDTO.getIdEnvio(), envioDTO.getFechaSalida(), envioDTO.getFechaLlegada(),
				envioDTO.isCumpleCondicionesCarga(), envioDTO.getEstado(), PedidoToEntity(envioDTO.getPedido()),
				envioDTO.getPrioridad());
		envio.setSucursalOrigen(envioDTO.getSucursalOrigen());
		envio.setSucursalDestino(envioDTO.getSucursalDestino());
		return envio;
	}

	public static Factura FacturaToEntity(FacturaDTO facturaDTO) {
		return new Factura(facturaDTO.getIdFactura(), PedidoToEntity(facturaDTO.getPedido()), facturaDTO.getPrecio());
	}

	public static Habilitado HabilitadoToEntity(HabilitadoDTO habilitadoDTO) {
		return new Habilitado(habilitadoDTO.getNombre(), habilitadoDTO.getDniHabilitado());
	}

	public static Particular ParticularToEntity(ParticularDTO particularDTO) {
		List<Habilitado> habilitados = new ArrayList<Habilitado>();
		for (HabilitadoDTO habilitado : particularDTO.getHabilitados()) {
			habilitados.add(HabilitadoToEntity(habilitado));
		}
		return new Particular(particularDTO.getIdCliente(), particularDTO.getNombre(), particularDTO.getDNI(),
				particularDTO.getApellido(), habilitados);
	}

	public static Pedido PedidoToEntity(PedidoDTO pedidoDTO) {
		List<Carga> cargas = new ArrayList<Carga>();
		for (CargaDTO cargaDTO : pedidoDTO.getCargas()) {
			cargas.add(CargaToEntity(cargaDTO));
		}
		Pedido pedido = new Pedido(pedidoDTO.getIdPedido(), DireccionToEntity(pedidoDTO.getDireccionCarga()),
				DireccionToEntity(pedidoDTO.getDireccionDestino()), pedidoDTO.getFechaCarga(),
				pedidoDTO.getHoraInicio(), pedidoDTO.getHoraFin(), pedidoDTO.getFechaMaxima(), cargas,
				pedidoDTO.getPrecio(), pedidoDTO.getSucursalDestino(), pedidoDTO.getSucursalOrigen(),
				pedidoDTO.isSolicitaTransporteDirecto(), pedidoDTO.isSolicitaAvionetaParticular(),
				ClienteToEntity(pedidoDTO.getCliente()));
		pedido.setSucursalOrigen(pedidoDTO.getSucursalOrigen());
		return pedido;
	}

	public static PlanDeMantenimiento PlanDeMantenimientoToEntity(PlanDeMantenimientoDTO planDeMantenimientoDTO) {
		return new PlanDeMantenimiento(planDeMantenimientoDTO.getIdPlanDeMantenimiento(),
				planDeMantenimientoDTO.getDiasProxControl(), planDeMantenimientoDTO.getDiasDemora(),
				planDeMantenimientoDTO.getKmProxControl());
	}

	public static PrecioVehiculo PrecioVehiculoToEntity(PrecioVehiculoDTO precioVehiculoDTO) {
		return new PrecioVehiculo(precioVehiculoDTO.getIdPrecioVehiculo(), precioVehiculoDTO.getTipoVehiculo(),
				precioVehiculoDTO.getPrecio());
	}

	public static Producto ProductoToEntity(ProductoDTO productoDTO) {
		return new Producto(productoDTO.getIdProducto(), productoDTO.getNombre(), productoDTO.getTipo());
	}

	public static Proveedor ProveedorToEntity(ProveedorDTO proveedorDTO) {
		return new Proveedor(proveedorDTO.getIdProveedor(), proveedorDTO.getCompania(),
				proveedorDTO.getTipoMercaderia());
	}

	public static Remito RemitoToEntity(RemitoDTO remitoDTO) {
		return new Remito(remitoDTO.getIdRemito(), PedidoToEntity(remitoDTO.getPedido()));
	}

	public static Ruta RutaToEntity(RutaDTO rutaDTO) {
		List<Trayecto> trayectos = new ArrayList<Trayecto>();
		for (TrayectoDTO trayecto : rutaDTO.getTrayectos()) {
			trayectos.add(TrayectoToEntity(trayecto));
		}
		return new Ruta(rutaDTO.getIdRuta(), trayectos, rutaDTO.getPrecio());
	}

	public static Sucursal SucursalToEntity(SucursalDTO sucursalDTO) {
		return new Sucursal(sucursalDTO.getIdSucursal(), sucursalDTO.getNombre(),
				DireccionToEntity(sucursalDTO.getUbicacion()), null);
	}

	public static Transporte TransporteToEntity(TransporteDTO transporteDTO) {
		return new Transporte(transporteDTO.getIdProveedor(), transporteDTO.getCompania(),
				transporteDTO.getTipoMercaderia(), transporteDTO.getTipoTransporte());
	}

	public static Trayecto TrayectoToEntity(TrayectoDTO trayectoDTO) {
		return new Trayecto(trayectoDTO.getIdTrayecto(), SucursalToEntity(trayectoDTO.getSucursalOrigen()),
				SucursalToEntity(trayectoDTO.getSucursalDestino()), trayectoDTO.getTiempo(), trayectoDTO.getKm(),
				trayectoDTO.getPrecio());
	}

	public static Vehiculo VehiculoToEntity(VehiculoDTO vehiculoDTO) {
		return new Vehiculo(vehiculoDTO.getIdVehiculo(), vehiculoDTO.getTipo(), vehiculoDTO.getVolumen(),
				vehiculoDTO.getPeso(), vehiculoDTO.getAncho(), vehiculoDTO.getAlto(), vehiculoDTO.getProfundidad(),
				vehiculoDTO.getTara(), vehiculoDTO.getKilometraje(), vehiculoDTO.getEstado(),
				vehiculoDTO.isEnGarantia(), vehiculoDTO.isTrabajoEspecifico(), vehiculoDTO.getFechaUltimoControl(),
				PlanDeMantenimientoToEntity(vehiculoDTO.getPlanDeMantenimiento()));
	}

	public static Viaje ViajeToEntity(ViajeDTO viajeDTO) {
		List<Envio> envios = new ArrayList<Envio>();
		for (EnvioDTO envioDTO : viajeDTO.getEnvios()) {
			envios.add(EnvioToEntity(envioDTO));
		}
		return new Viaje(viajeDTO.getIdViaje(), envios, viajeDTO.getFechaLlegada(),
				SucursalToEntity(viajeDTO.getSucursalOrigen()), SucursalToEntity(viajeDTO.getSucursalDestino()),
				viajeDTO.isFinalizado(), VehiculoToEntity(viajeDTO.getVehiculo()));
	}

}
