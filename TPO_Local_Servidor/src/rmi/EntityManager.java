package rmi;

import java.util.ArrayList;
import java.util.List;

import dto.CargaDTO;
import dto.ClienteDTO;
import dto.DireccionDTO;
import dto.EmpresaDTO;
import dto.EnvioDTO;
import dto.FacturaDTO;
import dto.ParticularDTO;
import dto.PedidoDTO;
import dto.PlanDeMantenimientoDTO;
import dto.RemitoDTO;
import dto.RutaDTO;
import dto.SucursalDTO;
import dto.TrayectoDTO;
import dto.VehiculoDTO;
import dto.VehiculoTerceroDTO;
import entities.Carga;
import entities.Cliente;
import entities.Direccion;
import entities.Empresa;
import entities.Envio;
import entities.Factura;
import entities.Particular;
import entities.Pedido;
import entities.PlanDeMantenimiento;
import entities.Remito;
import entities.Ruta;
import entities.Sucursal;
import entities.Trayecto;
import entities.Vehiculo;
import entities.VehiculoTercero;

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
		return new Empresa(empresaDTO.getIdCliente(), empresaDTO.getNombre(), empresaDTO.getCUIT(),
				empresaDTO.getTipo(), empresaDTO.getDetallePoliticas(), empresaDTO.getSaldoCuentaCorriente());
	}

	public static Envio EnvioToEntity(EnvioDTO envioDTO) {
		List<Pedido> pedidos = new ArrayList<Pedido>();
		for (PedidoDTO pedido : envioDTO.getPedidos()) {
			pedidos.add(PedidoToEntity(pedido));
		}
		Envio envio = new Envio(envioDTO.getIdEnvio(), envioDTO.getFechaSalida(), envioDTO.getFechaLlegada(),
				envioDTO.isCumpleCondicionesCarga(), envioDTO.getEstado(), pedidos, envioDTO.getPrioridad(),
				SucursalToEntity(envioDTO.getSucursalOrigen()), SucursalToEntity(envioDTO.getSucursalDestino()),
				envioDTO.getVehiculoId());
		return envio;
	}

	public static Factura FacturaToEntity(FacturaDTO facturaDTO) {
		return new Factura(facturaDTO.getIdFactura(), PedidoToEntity(facturaDTO.getPedido()), facturaDTO.getPrecio());
	}

	public static Particular ParticularToEntity(ParticularDTO particularDTO) {
		return new Particular(particularDTO.getIdCliente(), particularDTO.getNombre(), particularDTO.getDNI(),
				particularDTO.getApellido());
	}

	public static Pedido PedidoToEntity(PedidoDTO pedidoDTO) {
		List<Carga> cargas = new ArrayList<Carga>();
		for (CargaDTO cargaDTO : pedidoDTO.getCargas()) {
			cargas.add(CargaToEntity(cargaDTO));
		}
		Pedido pedido = new Pedido(pedidoDTO.getIdPedido(), DireccionToEntity(pedidoDTO.getDireccionCarga()),
				DireccionToEntity(pedidoDTO.getDireccionDestino()), pedidoDTO.getFechaCarga(),
				pedidoDTO.getHoraInicio(), pedidoDTO.getHoraFin(), pedidoDTO.getFechaMaxima(), cargas,
				pedidoDTO.getPrecio(), pedidoDTO.getSucursalOrigenId(), pedidoDTO.getSucursalDestinoId(),
				pedidoDTO.getSucursalActualId(), pedidoDTO.isSolicitaTransporteDirecto(),
				pedidoDTO.isSolicitaAvionetaParticular(), ClienteToEntity(pedidoDTO.getCliente()),
				pedidoDTO.getEstado());
		return pedido;
	}

	public static PlanDeMantenimiento PlanDeMantenimientoToEntity(PlanDeMantenimientoDTO planDeMantenimientoDTO) {
		return new PlanDeMantenimiento(planDeMantenimientoDTO.getIdPlanDeMantenimiento(),
				planDeMantenimientoDTO.getDiasProxControl(), planDeMantenimientoDTO.getDiasDemora(),
				planDeMantenimientoDTO.getKmProxControl());
	}

	public static VehiculoTercero VehiculoTerceroToEntity(VehiculoTerceroDTO vehiculoTerceroDTO) {
		if (vehiculoTerceroDTO.getPedidos()!=null){
			List<Pedido> pedidos = new ArrayList<Pedido>();
			for (PedidoDTO pedidoDto : vehiculoTerceroDTO.getPedidos()) {
				pedidos.add(PedidoToEntity(pedidoDto));
			}
			return new VehiculoTercero(vehiculoTerceroDTO.getIdVehiculoTercero(), vehiculoTerceroDTO.getTipoVehiculo(),
					vehiculoTerceroDTO.getPrecio(), vehiculoTerceroDTO.getEstado(), vehiculoTerceroDTO.getFechaLlegada(),
					pedidos);
		}else
			return new VehiculoTercero(vehiculoTerceroDTO.getIdVehiculoTercero(), vehiculoTerceroDTO.getTipoVehiculo(),
					vehiculoTerceroDTO.getPrecio(), vehiculoTerceroDTO.getEstado(), vehiculoTerceroDTO.getFechaLlegada(),
					null);
			
		
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
		if(sucursalDTO.getPedidos()!=null&&sucursalDTO.getUbicacion()!=null){
			List<Pedido> pedidos = new ArrayList<Pedido>();
			for (PedidoDTO pedido : sucursalDTO.getPedidos()) {
				pedidos.add(PedidoToEntity(pedido));
			}
			return new Sucursal(sucursalDTO.getIdSucursal(), sucursalDTO.getNombre(),
					DireccionToEntity(sucursalDTO.getUbicacion()),pedidos);
		}
		else if(sucursalDTO.getPedidos()!=null&&sucursalDTO.getUbicacion()==null){
			List<Pedido> pedidos = new ArrayList<Pedido>();
			for (PedidoDTO pedido : sucursalDTO.getPedidos()) {
				pedidos.add(PedidoToEntity(pedido));
			}
			return new Sucursal(sucursalDTO.getIdSucursal(), sucursalDTO.getNombre(),
					null, pedidos);
		}
		else if(sucursalDTO.getPedidos()==null&&sucursalDTO.getUbicacion()!=null)
			return new Sucursal(sucursalDTO.getIdSucursal(), sucursalDTO.getNombre(),
					DireccionToEntity(sucursalDTO.getUbicacion()), null);
		else
			return new Sucursal(sucursalDTO.getIdSucursal(), sucursalDTO.getNombre(),
					null, null);
			
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
				vehiculoDTO.getSucursalIdActual(), PlanDeMantenimientoToEntity(vehiculoDTO.getPlanDeMantenimiento()));
	}

}
