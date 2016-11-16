package rmi;

import java.io.File;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import net.sourceforge.jtds.jdbc.DateTime;
import Strategy.PoliticaEspecificidad;
import Strategy.PoliticaGarantia;
import Strategy.PoliticaGeneral;
import Strategy.PoliticaMantenimiento;
import dao.HibernateDAO;
import dto.CargaDTO;
import dto.ClienteDTO;
import dto.DireccionDTO;
import dto.EmpresaDTO;
import dto.EnvioDTO;
import dto.FacturaDTO;
import dto.HabilitadoDTO;
import dto.MapaDeRutaDTO;
import dto.ParticularDTO;
import dto.PedidoDTO;
import dto.PlanDeMantenimientoDTO;
import dto.PrecioVehiculoDTO;
import dto.ProductoDTO;
import dto.ProveedorDTO;
import dto.RemitoDTO;
import dto.RutaDTO;
import dto.SeguroDTO;
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
import entities.Transporte;
import entities.Trayecto;
import entities.Vehiculo;
import entities.Sucursal;
import entities.Viaje;


public class RemoteObject extends UnicastRemoteObject implements RemoteInterface {

	private static final long serialVersionUID = 1L;
	private static HibernateDAO hbtDAO;
	public MapaDeRutaDTO mapadeRuta;
	private PoliticaMantenimiento politicaMantenimiento;
	
	public void InicializarMapaDeRuta()
	{
		cargarMapaDeRuta();
	}
	 
	
	public RemoteObject() throws RemoteException {
		super();
		hbtDAO = HibernateDAO.getInstancia();
	}
	
	//---------------------------------- METODOS PRIVADOS ----------------------------------------
	private Carga CargaToEntity(CargaDTO cargaDTO){
		return new Carga(cargaDTO.getIdCarga(),cargaDTO.getPeso(), cargaDTO.getAncho(), cargaDTO.getAlto(), cargaDTO.getProfundidad(),
				cargaDTO.getVolumen(), cargaDTO.getFragilidad(), cargaDTO.getTratamiento(), cargaDTO.getApilable(),
				cargaDTO.isRefrigerable(), cargaDTO.getCondiciones(), cargaDTO.isDespachado(),
				cargaDTO.getTipoMercaderia());
	}
	
	private Cliente ClienteToEntity (ClienteDTO clienteDTO){
		return new Cliente(clienteDTO.getIdCliente(),clienteDTO.getNombre());
	}
	
	private Direccion DireccionToEntity (DireccionDTO direccionDTO){
		return new Direccion(direccionDTO.getIdDireccion(),direccionDTO.getCalle(), direccionDTO.getNumero(), direccionDTO.getPiso(), 
								direccionDTO.getDepartamento(), direccionDTO.getCP());
	}
	
	private Empresa EmpresaToEntity(EmpresaDTO empresaDTO){
		List<Producto> productos = new ArrayList<Producto>();
		for (ProductoDTO producto : empresaDTO.getProductos())
			productos.add(ProductoToEntity(producto));		
		return new Empresa(empresaDTO.getIdCliente(),empresaDTO.getNombre(),empresaDTO.getCUIT(), empresaDTO.getTipo(), empresaDTO.getDetallePoliticas(), 
				productos, empresaDTO.getSaldoCuentaCorriente());
	}
	
	private Envio EnvioToEntity(EnvioDTO envioDTO){
		 Envio e=new Envio (envioDTO.getIdEnvio(),envioDTO.getFechaSalida(), envioDTO.getFechaLlegada(), envioDTO.isCumpleCondicionesCarga(),
				envioDTO.getEstado(), PedidoToEntity(envioDTO.getPedido()), envioDTO.getPrioridad());
		
		 e.setSucursalOrigen(envioDTO.getSucursalOrigen());
		 return e;
	}
		
	private Factura FacturaToEntity(FacturaDTO facturaDTO){
		return new Factura (facturaDTO.getIdFactura(),PedidoToEntity(facturaDTO.getPedido()), facturaDTO.getPrecio());
	}
	
	private Habilitado HabilitadoToEntity(HabilitadoDTO habilitadoDTO){
		return new Habilitado(habilitadoDTO.getNombre(), habilitadoDTO.getDniHabilitado());
	}
	
	private Particular ParticularToEntity(ParticularDTO particularDTO){
		List<Habilitado> habilitados = new ArrayList<Habilitado>();
		for (HabilitadoDTO habilitado : particularDTO.getHabilitados())
			habilitados.add(HabilitadoToEntity(habilitado));
		return new Particular(particularDTO.getIdCliente(),particularDTO.getNombre(),particularDTO.getDNI(), particularDTO.getApellido(), habilitados);
	}
		
	private Pedido PedidoToEntity(PedidoDTO pedidoDTO){
		List<Carga> cargas = new ArrayList<Carga>();
		for(CargaDTO cargaDTO : pedidoDTO.getCargas())
			cargas.add(CargaToEntity(cargaDTO));
		
		Pedido p=new Pedido(pedidoDTO.getIdPedido(),DireccionToEntity(pedidoDTO.getDireccionCarga()), DireccionToEntity(pedidoDTO.getDireccionDestino()),
				pedidoDTO.getFechaCarga(), pedidoDTO.getHoraInicio(), pedidoDTO.getHoraFin(), pedidoDTO.getFechaMaxima(),
				cargas, pedidoDTO.getPrecio(), pedidoDTO.getSucursalDestino(), pedidoDTO.getSucursalOrigen(),
				pedidoDTO.isSolicitaTransporteDirecto(),
				pedidoDTO.isSolicitaAvionetaParticular(), ClienteToEntity(pedidoDTO.getCliente()));
		p.setSucursalOrigen(pedidoDTO.getSucursalOrigen());
		return p;
	}
	

	private PlanDeMantenimiento PlanDeMantenimientoToEntity (PlanDeMantenimientoDTO planDeMantenimientoDTO){
		return new PlanDeMantenimiento(planDeMantenimientoDTO.getIdPlanDeMantenimiento(),planDeMantenimientoDTO.getDiasProxControl(),
				planDeMantenimientoDTO.getDiasDemora(), planDeMantenimientoDTO.getKmProxControl());
	}
	
	private PrecioVehiculo PrecioVehiculoToEntity(PrecioVehiculoDTO precioVehiculoDTO){
		return new PrecioVehiculo(precioVehiculoDTO.getIdPrecioVehiculo(),precioVehiculoDTO.getTipoVehiculo(), precioVehiculoDTO.getPrecio());
	}
	
	private Producto ProductoToEntity(ProductoDTO productoDTO){
		return new Producto(productoDTO.getIdProducto(),productoDTO.getNombre(), productoDTO.getTipo());	
	}

	private Proveedor ProveedorToEntity(ProveedorDTO proveedorDTO){
		return new Proveedor(proveedorDTO.getIdProveedor(),proveedorDTO.getCompania(), proveedorDTO.getTipoMercaderia());
	}
	
	private Remito RemitoToEntity(RemitoDTO remitoDTO){
		return new Remito (remitoDTO.getIdRemito(),PedidoToEntity(remitoDTO.getPedido()));
	}
	
	private Ruta RutaToEntity(RutaDTO rutaDTO){
		List<Trayecto> trayectos = new ArrayList<Trayecto>();
		for (TrayectoDTO trayecto : rutaDTO.getTrayectos())
			trayectos.add(TrayectoToEntity(trayecto));
		return new Ruta(rutaDTO.getIdRuta(),trayectos, rutaDTO.getPrecio());			
	}
	

	private Sucursal SucursalToEntity (SucursalDTO sucursalDTO){
		return new Sucursal (sucursalDTO.getIdSucursal(),sucursalDTO.getNombre(), DireccionToEntity(sucursalDTO.getUbicacion()), null);
	}

	private Transporte TransporteToEntity(TransporteDTO transporteDTO){
		return new Transporte(transporteDTO.getIdProveedor(),transporteDTO.getCompania(),transporteDTO.getTipoMercaderia(),transporteDTO.getTipoTransporte());
	}
	
	private Trayecto TrayectoToEntity(TrayectoDTO trayectoDTO){
		return new Trayecto(trayectoDTO.getIdTrayecto(),SucursalToEntity(trayectoDTO.getSucursalOrigen()), SucursalToEntity(trayectoDTO.getSucursalDestino()),
				trayectoDTO.getTiempo(), trayectoDTO.getKm(), trayectoDTO.getPrecio());	
	}
	
	private Vehiculo VehiculoToEntity(VehiculoDTO vehiculoDTO){

		return new Vehiculo (vehiculoDTO.getIdVehiculo(),vehiculoDTO.getTipo(), vehiculoDTO.getVolumen(), vehiculoDTO.getPeso(),
				vehiculoDTO.getAncho(), vehiculoDTO.getAlto(), vehiculoDTO.getProfundidad(), vehiculoDTO.getTara(),
				vehiculoDTO.getKilometraje(), vehiculoDTO.getEstado(),  vehiculoDTO.isEnGarantia(), vehiculoDTO.isTrabajoEspecifico(), 
				vehiculoDTO.getFechaUltimoControl(), PlanDeMantenimientoToEntity(vehiculoDTO.getPlanDeMantenimiento()));

	}

	private Viaje ViajeToEntity(ViajeDTO viajeDTO){
		List<Envio> envios = new ArrayList<Envio>();
		for(EnvioDTO envioDTO: viajeDTO.getEnvios())
			envios.add(EnvioToEntity(envioDTO));
		return new Viaje (viajeDTO.getIdViaje(),envios, viajeDTO.getFechaLlegada(), SucursalToEntity(viajeDTO.getSucursalOrigen()),
				SucursalToEntity(viajeDTO.getSucursalDestino()), viajeDTO.isFinalizado(),VehiculoToEntity(viajeDTO.getVehiculo()));
	}
	
	
	
	
	//--------------------------------------- ABMs ----------------------------------------------
	public void altaPedido(PedidoDTO pedidoDTO){
		
		Pedido pedido = new Pedido();
		SucursalDTO o=obtenerSucursal(pedidoDTO.getSucursalOrigen());
		SucursalDTO d=obtenerSucursal(pedidoDTO.getSucursalDestino());
		pedidoDTO.setPrecio(calcularPrecio(o,d));
		pedido = PedidoToEntity(pedidoDTO);
		
		hbtDAO.guardar(pedido);
	}
	@Override
	public void altaCliente(ClienteDTO clienteDto) throws RemoteException {
		Cliente cliente=new Cliente();
		cliente=ClienteToEntity(clienteDto);
		hbtDAO.guardar(cliente);
	}
	public float calcularPrecio(SucursalDTO o,SucursalDTO d)
	{
		MapaDeRutaDTO mp = mapadeRuta;
		cargarMapaDeRuta();
		RutaDTO r=obtenerMejorRuta(o, d);
		float precio=0;
		for(TrayectoDTO t:r.getTrayectos())
		{
			precio=precio+t.getPrecio();
		}
		return precio;
		 
	}
	public void altaEnvio (EnvioDTO envioDTO){
		hbtDAO.guardar(EnvioToEntity(envioDTO));		
	}
	public void altaVehiculo(VehiculoDTO vehiculoDTO){
		hbtDAO.guardar(VehiculoToEntity(vehiculoDTO));
	}
	public void altaPlanMantenimiento(PlanDeMantenimientoDTO planDeMantenimientoDTO){
		hbtDAO.guardar(PlanDeMantenimientoToEntity(planDeMantenimientoDTO));
	}
	public void altaViaje(ViajeDTO viajeDTO){
		hbtDAO.guardar(ViajeToEntity(viajeDTO));
	}
	public void altaCarga(CargaDTO cargaDTO){
		hbtDAO.guardar(CargaToEntity(cargaDTO));
	}
	public void altaRemito(RemitoDTO remitoDTO){
		hbtDAO.guardar(RemitoToEntity(remitoDTO));
	}
	public void altaFactura(FacturaDTO facturaDTO){
		hbtDAO.guardar(FacturaToEntity(facturaDTO));
	}
	public void altaTransporte(TransporteDTO transporteDTO){
		hbtDAO.guardar(TransporteToEntity(transporteDTO));
	}
	public void altaProveedor(ProveedorDTO proveedorDTO){
		hbtDAO.guardar(ProveedorToEntity(proveedorDTO));
	}
	
	public List<ViajeDTO> ObtenerViajesDeCliente(int idCliente){
		return hbtDAO.obtenerViajesDeCliente(idCliente);
	}
	public float SeleccionarViaje(int idViaje){
		return hbtDAO.seleccionarViaje(idViaje);	
	}
	public List<SucursalDTO> obtenerSucursales() throws RemoteException {
		return hbtDAO.obtenerSucursales();
	}
	public List<ViajeDTO> obtenerViajes() throws RemoteException {
		return hbtDAO.obtenerViajes();
	}
	private boolean existeClienteParticular(int DNI)
	{
		return (hbtDAO.obtenerClienteParticular(DNI) !=null);
	}
	public List<SeguroDTO> obtenerSegurosParaCarga(String tipoMercaderia)
	{
		return hbtDAO.obtenerSegurosParaCarga(tipoMercaderia);
	}
	public List<VehiculoDTO> obtenerVehiculos()
	{
		return hbtDAO.obtenerVehiculos();
	}
	/*Aca mi idea era, ves una pantalla de vehiculos, si queres controlar el PM de uno, 
	 * apretas en controlar o algo asi y se ejecuta esta funcion, devuelve true si necesitaba 
	 * llevarlo a mantenimiento*/
	/*Solo se pueden llevar a mantenimiento los que estan en deposito,
	 *  los que estan en Uso o en Mantenimiento no.*/
	
	/*
	La pantalla creo que es para cuando se rompe el vehiculo
	 Y despues para mi con una funcion que se ejecute todos los dias y verifique cadael vehiculo
	 ya estaria, la agregue como comentarios cualquier 
	cosa la descomentamos y listo. La clase controlador tambien se usa para eso
	Igualmente use esta funcion para controlar
		*/
	
	public boolean ControlarVehiculo(VehiculoDTO vehiculoDTO)
	{
		/*Obtengo la fecha en la que deberian hacerle el mantenimiento al vehiculo*/
		Calendar c = Calendar.getInstance();
		c.setTime(vehiculoDTO.getFechaUltimoControl()); 
		c.add(Calendar.DATE, vehiculoDTO.getPlanDeMantenimiento().getDiasProxControl()); 
		Date fecha = c.getTime();
		
		boolean Boolean=false;
		if(vehiculoDTO.getEstado().equals("En Deposito"))
		{	System.out.println(vehiculoDTO.getKilometraje()+vehiculoDTO.getPlanDeMantenimiento().getKmProxControl());
			if(vehiculoDTO.getKilometraje()>=vehiculoDTO.getPlanDeMantenimiento().getKmProxControl())
			{	
				Boolean=true;
				if(vehiculoDTO.isEnGarantia())
				{
					politicaMantenimiento = new PoliticaGarantia();
					politicaMantenimiento.mandarAMantenimiento(vehiculoDTO);
				}
				else if(vehiculoDTO.isTrabajoEspecifico())
				{
					politicaMantenimiento = new PoliticaEspecificidad();
					politicaMantenimiento.mandarAMantenimiento(vehiculoDTO);
				}
				else
				{
					politicaMantenimiento = new PoliticaGeneral();
					politicaMantenimiento.mandarAMantenimiento(vehiculoDTO);
				}
				vehiculoDTO.getPlanDeMantenimiento().setKmProxControl(vehiculoDTO.getKilometraje()+200);
				vehiculoDTO.getPlanDeMantenimiento().setDiasProxControl(60);
				hbtDAO.modificar(VehiculoToEntity(vehiculoDTO));
			}
			/*Me fijo si la fecha calculada al principio esta antes que la de hoy, si lo esta, mando mantenimiento*/
			else if(fecha.before(new Date()))
			{
				Boolean=true;
				if(vehiculoDTO.isEnGarantia())
				{
					politicaMantenimiento = new PoliticaGarantia();
					politicaMantenimiento.mandarAMantenimiento(vehiculoDTO);
				}
				else if(vehiculoDTO.isTrabajoEspecifico())
				{
					politicaMantenimiento = new PoliticaEspecificidad();
					politicaMantenimiento.mandarAMantenimiento(vehiculoDTO);
				}
				else
				{
					politicaMantenimiento = new PoliticaGeneral();
					politicaMantenimiento.mandarAMantenimiento(vehiculoDTO);
				}
				vehiculoDTO.getPlanDeMantenimiento().setKmProxControl(vehiculoDTO.getKilometraje()+200);
				vehiculoDTO.getPlanDeMantenimiento().setDiasProxControl(60);
				hbtDAO.modificar(VehiculoToEntity(vehiculoDTO));
			}
		}
		return Boolean;
	}
	
	public VehiculoDTO obtenerVehiculo(VehiculoDTO v) {
		return hbtDAO.obtenerVehiculo(v.getIdVehiculo());
		
	}
 
	public ViajeDTO obtenerViajePorVehiculo(VehiculoDTO vehiculo) {
		return hbtDAO.obtenerViajePorVehiculo(vehiculo);
	}
 

	public void actualiarViaje(ViajeDTO viajeDTO) {
	   hbtDAO.updateViaje(ViajeToEntity(viajeDTO));
		
	}
	
	private List<PedidoDTO> obtenerPedidos(ClienteDTO c)
	{
		return hbtDAO.obtenerPedidosDeCliente(c.getIdCliente());
	}
	private List<ViajeDTO> obtenerViajesPorPedidos(List<PedidoDTO> pedidosDTO)
	{
		 return hbtDAO.obtenerViajesDePedidos(pedidosDTO);
		
	}
	
	public List<ViajeDTO> controlarPedidosDeCliente(ClienteDTO c)
	{
		List<ViajeDTO> viajesDTO= new ArrayList<ViajeDTO>();
		List<PedidoDTO> pedidosDTO=obtenerPedidos(c);
		List<SucursalDTO> sucursales=hbtDAO.obtenerSucursales();
		/*for(SucursalDTO s:sucursales)
		{
			viajesDTo.add(obtenerViajesPorPedidos());
		}
		*/
		viajesDTO=obtenerViajesPorPedidos(pedidosDTO);
		return viajesDTO;
	}
	//QUE RECIBA UN int como los minutos a demorar sino creo que es mas complicado
	public void demorarViaje(ViajeDTO viajeDTO,int m)
	{
		
		long milisegundos=60000;
		Date auxiliar= viajeDTO.getFechaLlegada();
		long minutosAux=m*milisegundos;
		Date auxiliar2=new Date(auxiliar.getTime() + minutosAux);
		viajeDTO.setFechaLlegada(auxiliar2);
		 hbtDAO.updateViaje(ViajeToEntity(viajeDTO));
	}
	//En realidad lo unico que tiene es el id que le paso el empleado
	public ViajeDTO obtenerViaje(ViajeDTO viajeDTO)
	{
		return hbtDAO.obtenerViaje(viajeDTO.getIdViaje());
	}
	//Hay que ver como manejamos el idSucursal porque se supone que el sistema conoce 
	//quien le hace el pedido entonces usa los trayectos cuyo origen es la sucursal de origen
	//
	
	private List<TrayectoDTO> obtenerTodosLosTrayectos(SucursalDTO origen,SucursalDTO destino) {
		List<TrayectoDTO> trayectos=new ArrayList<TrayectoDTO>();
		
		 for(RutaDTO r: mapadeRuta.getRutas())
		 {
			 for(TrayectoDTO t:r.getTrayectos())
			 {
				 if(t.getSucursalOrigen()==origen && t.getSucursalDestino()==destino)
				 {
					 trayectos.add(t);
				 }
			 }
		 }
		 return trayectos;
	}
	public void cargarMapaDeRuta() {
		 List<RutaDTO>rutas =hbtDAO.obtenerRutas();
		 mapadeRuta=new MapaDeRutaDTO();
		 mapadeRuta.setRutas(rutas);
	}
	
	//el Trayecto pasado como parametro tiene los nuevos valores de km , tiempo, y precio
	//Lo habia pensado como que solo se llama para decir que un trayecto no se puede hacer pero el CU dice que se modifica
	//el precio, km ,etc entonces lo cambie. Y agregue que si el trayecto ahora es mejor que antes entonces no cambio nada solamente 
	//actualizo los tiempos
	
	public void actualizarViajes(TrayectoDTO trayDTO,SucursalDTO sucursalDTO)
	{
		cargarMapaDeRuta();
		 TrayectoDTO aux=new TrayectoDTO();
		 float btiempo=99999999;
		// aux=hbtDAO.obtenerTrayecto(trayDTO);
		
			
		 actualizarMapaDeRutas(trayDTO);
		 List<RutaDTO> rutas= new ArrayList<RutaDTO>();
		 rutas=obtenerTodasLasRutasDirectas(trayDTO.getSucursalOrigen());
		  
		aux=new TrayectoDTO();
		 for(RutaDTO r:rutas)
		 {
		 //Se que tiene un solo trayecto porque eso es lo que devuelve obtenerTodasLasRutasDirectas
		 TrayectoDTO t =r.getTrayectos().get(0);;
		 float tiempo=0;
		
		 //Veo cual es la sucursal mas cercana en TIEMPO o si me conviene volver a la sucursal de origen
		  
				 tiempo= t.getTiempo();
				 if(tiempo<btiempo)
				 {
					 aux=t;
					 btiempo=tiempo;	
				 }
			
		 }
		 
		 rutas= obtenerTodasLasRutasDirectas(trayDTO.getSucursalDestino());
		 for(RutaDTO r: rutas)
		 {
			 float tiempo=0;
			 TrayectoDTO t=r.getTrayectos().get(0);
		 if(t.getSucursalDestino()==trayDTO.getSucursalOrigen())
		 {
			 if(tiempo<btiempo)
			 {
				 aux=t;
				 btiempo=tiempo;	
			 } 
		 }
		 }
		 
		
		 //DEBERIA OBTENER LOS VIAJES SOLO DELA SUCURSAL DE ORIGEN PERO DEVUELVE TODOS
		 //ASIQ VERIFICO QUE LA SUCURSAL ORIGEN SEA LA INDICADA
		List<ViajeDTO> viajesDTO= hbtDAO.obtenerViajes();
		for(ViajeDTO v:viajesDTO)
		{
			if(v.getSucursalOrigen().getIdSucursal()==trayDTO.getSucursalOrigen().getIdSucursal() && v.getSucursalDestino().getIdSucursal()==trayDTO.getSucursalDestino().getIdSucursal())
			{
			v.setSucursalDestino(aux.getSucursalDestino());
			long m=(long) aux.getTiempo();
			long milisegundos=60000;
			Date auxiliar= Calendar.getInstance().getTime();
			long minutosAux=m*milisegundos;
			Date auxiliar2=new Date(auxiliar.getTime() + minutosAux);
			v.setFechaLlegada(auxiliar2);
			for(EnvioDTO e: v.getEnvios())
			{
				e.setFechaLlegada(auxiliar2);
				hbtDAO.modificar(EnvioToEntity(e));
			}
			hbtDAO.modificar(ViajeToEntity(v));
			}
			
		}
	
	}
	public MapaDeRutaDTO getMapa()
	{
		return mapadeRuta;
	}

private List<RutaDTO> obtenerTodasLasRutasDirectas(SucursalDTO sucursalOrigen) {
	MapaDeRutaDTO mp = mapadeRuta;	 
	List<RutaDTO> rutas=new ArrayList<RutaDTO>();
 	for(RutaDTO r: mp.getRutas())
	{
		if(r.getOrigen().getIdSucursal()==sucursalOrigen.getIdSucursal()  && r.getTrayectos().size()==1)
		{
			 rutas.add(r);
		}
	}
	
	return rutas;
	}

	public RutaDTO obtenerMejorRuta(SucursalDTO origen,SucursalDTO destino) 
	{
		MapaDeRutaDTO mp = mapadeRuta;
		cargarMapaDeRuta();
		float precioMin=999999999;
		RutaDTO ruta=new RutaDTO();
		int km=99999999;
		 
		for(RutaDTO r: mp.getRutas())
		{
			
		
			if(r.getOrigen().getIdSucursal()==origen.getIdSucursal() && r.getDestino().getIdSucursal()==destino.getIdSucursal())
			{
				int kmAux=r.calcularKm();
				if(kmAux<km)
				{
					km=kmAux;
					ruta=r;
				}
				else if(kmAux==km)
				{
				if(r.getPrecio()<precioMin)
				{
					precioMin=r.getPrecio();
					ruta=r;
					
				}
			}
		}
		}
		return ruta;
		
	}
	//
	public List<TransporteDTO> obtenerTransportesDeTerceros(CargaDTO c, TransporteDTO tr)
	{
		return hbtDAO.obtenerTransportesDeTerceros(c,tr);
	}
	
	
	public void actualizarMapaDeRutas(TrayectoDTO t) {
		
		t.setSucursalOrigen(hbtDAO.obtenerSucursal(t.getSucursalOrigen()));
		t.setSucursalDestino(hbtDAO.obtenerSucursal(t.getSucursalDestino()));
		hbtDAO.modificar(TrayectoToEntity(t));
		cargarMapaDeRuta();
	}

	public void actualizarPedido(PedidoDTO pedido) {
		hbtDAO.modificar(PedidoToEntity(pedido));
	}

	public List<PedidoDTO> obtenerPedidos() {
		return hbtDAO.obtenerPedidos();
	}

	
	public List<ClienteDTO> obtenerClientes() {
		return hbtDAO.obtenerClientes();
	}
	
	public List<EmpresaDTO> obtenerClientesEmpresa() {
		return hbtDAO.obtenerClientesEmpresa();
	}
	public List<ParticularDTO> obtenerClientesParticular() {
		return hbtDAO.obtenerClientesParticular();
	}

	public List<CargaDTO> obtenerCargasDeUnPedido(PedidoDTO pedido) {
		return hbtDAO.obtenerCargasDeUnPedido(pedido);
	}
	public String validarCredenciales(String username, String password) {
		// TODO Auto-generated method stub
		
		return hbtDAO.validarCredenciales(username, password);
	}

 
	public void enviar()
	{
		List<PedidoDTO> pedidos=hbtDAO.obtenerPedidos();
		List<EnvioDTO> envios=new ArrayList<EnvioDTO>();
		List<EnvioDTO> enviosAux=new ArrayList<EnvioDTO>();
		pedidos=ordenarPedidosPorPrioridad(pedidos);
	//ORDERNAR PEDIDOS por prioridad y si la carga no esta despacha entonces es que no llego al destino final
		Date fechaLlegada=Calendar.getInstance().getTime();
		List<VehiculoDTO>vehiculos= new ArrayList<VehiculoDTO>();
		ViajeDTO viaje=new ViajeDTO();
		SucursalDTO sucursalOrigen=new SucursalDTO();
		float carga=0;
		RutaDTO ruta=new RutaDTO();
		SucursalDTO sucursal=new SucursalDTO();
		MapaDeRutaDTO mp = mapadeRuta;
		cargarMapaDeRuta();
		boolean nul=false;
		float tiempo=0;
		for(PedidoDTO p:pedidos)
		{
			 nul=false;
		EnvioDTO envio =hbtDAO.obtenerEnvioDePedido(p.getIdPedido());
		if(envio==null)
		{
			envio=new EnvioDTO();
			 
			  sucursalOrigen=obtenerSucursal(p.getSucursalOrigen());
			  nul=true;
			  envio.setEstado("listo");
			
		}
		 if(!envio.getEstado().equals("despachado"))
		 {
         if(envio.getEstado().equals("listo"))
		{
        	 EnvioDTO envio2=envio;
        	 if(!nul)
        	 {
        	 ViajeDTO viajeAux=hbtDAO.obtenerViajeDeEnvio(envio.getIdEnvio());
 			hbtDAO.borrar(ViajeToEntity(viajeAux));
        	 }
			envio2.setCumpleCondicionesCarga(true);
			envio2.setPedido(p);
			envio2.setFechaSalida(Calendar.getInstance().getTime());
			envio2.setPrioridad(1);
			envio2.setFechaLlegada(fechaLlegada);
			 if(!nul)
				sucursalOrigen=obtenerSucursal(envio2.getSucursalOrigen());
				
			envio2.setSucursalOrigen( String.valueOf(sucursalOrigen.getIdSucursal()));
			envio=envio2;			
		}
		    
		    carga=0;
			for(CargaDTO c:p.getCargas())
			{
			carga=c.getVolumen()+carga;	
			}
			vehiculos=obtenerVehiculosListos();
			if(vehiculos!=null)
			{
			VehiculoDTO v=vehiculos.get(0);
			float volu=(v.getVolumen()*70)/100;
			if(carga>volu && carga<v.getVolumen())
			{
				v.setEstado("En uso");
			List<EnvioDTO>e=new ArrayList<EnvioDTO>();
			envio.setEstado("despachado");
			envio.setPedido(p);
			envios.add(envio);
			e.add(envio);
			viaje.setEnvios(e);
			viaje.setSucursalOrigen(sucursalOrigen);
			 
			sucursal=obtenerSucursal(p.getSucursalDestino());
			ruta=obtenerMejorRuta(sucursalOrigen,sucursal);
			viaje.setSucursalDestino(ruta.getTrayectos().get(0).getSucursalDestino());		
			viaje.setVehiculo(v);
			Date llegada=Calendar.getInstance().getTime();
			 
			for(TrayectoDTO t:ruta.getTrayectos())
			{
				tiempo=t.getTiempo()+tiempo;
			}
			long m=(long) tiempo;
			long milisegundos=60000;
			Date auxiliar= Calendar.getInstance().getTime();
			long minutosAux=m*milisegundos;
			Date auxiliar2=new Date(auxiliar.getTime() + minutosAux);
			viaje.setFechaLlegada(auxiliar2);
			m=(long) ruta.getTrayectos().get(0).getTiempo();
			milisegundos=60000;
			auxiliar= Calendar.getInstance().getTime();
			minutosAux=m*milisegundos;
			auxiliar2=new Date(auxiliar.getTime() + minutosAux);
			viaje.setFinalizado(false);
			envio.setFechaLlegada(auxiliar2);
			v.setEstado("En uso");
			
			
			hbtDAO.modificar(VehiculoToEntity(v));
			/*
		 
				hbtDAO.modificar(EnvioToEntity(envio));
				else
					*/
			//hbtDAO.guardar(EnvioToEntity(envio));
			hbtDAO.guardar(ViajeToEntity(viaje));
		
			}
			
			else{
				 
				envio.setPedido(p);
				enviosAux.add(envio);
				
			}
			
		    }
		 }
		}
		if(enviosAux.size()!=0)
		{
			List<EnvioDTO>en=new ArrayList<EnvioDTO>();
			float volu,cargaAux;
			volu=cargaAux=0;
			vehiculos=obtenerVehiculos();
			List<SucursalDTO> sucursales=hbtDAO.obtenerSucursales();
			for(SucursalDTO sOrigen:sucursales)
			{
			for(SucursalDTO s:sucursales)
			{
			for(VehiculoDTO v:vehiculos)
			{
				if(v.getEstado().equals("En Deposito"))
				{
					carga=0;
				  volu=(v.getVolumen()*70)/100;
				  en=new ArrayList<EnvioDTO>();
				for(EnvioDTO e:enviosAux)
				{
					 
					cargaAux=0;
					
					if(obtenerSucursal(e.getSucursalOrigen())==sOrigen && proxDestino(e.getPedido())==s)
					{
						for(CargaDTO c:e.getPedido().getCargas())
						{
							cargaAux=carga+c.getVolumen();
							if(cargaAux>volu && cargaAux<v.getVolumen())
							{
								carga=c.getVolumen()+carga;	
								en.add(e);
							}
						}
					}
				   
				}
				
				if(carga>volu && carga<v.getVolumen())
				{
			    
				sucursalOrigen=sOrigen;
				viaje.setEnvios(en);
				viaje.setSucursalOrigen(sucursalOrigen);
				viaje.setSucursalDestino(s);	
				viaje.setVehiculo(v);
				Date llegada=Calendar.getInstance().getTime();
				 
				for(TrayectoDTO t:ruta.getTrayectos())
				{
					tiempo=t.getTiempo()+tiempo;
				}
				long m=(long) tiempo;
				long milisegundos=60000;
				Date auxiliar= Calendar.getInstance().getTime();
				long minutosAux=m*milisegundos;
				Date auxiliar2=new Date(auxiliar.getTime() + minutosAux);
				viaje.setFechaLlegada(auxiliar2);
				viaje.setFinalizado(false);
				
				for(EnvioDTO e:en)
				{
					m=(long) ruta.getTrayectos().get(0).getTiempo();
					milisegundos=60000;
					auxiliar= Calendar.getInstance().getTime();
					minutosAux=m*milisegundos;
					auxiliar2=new Date(auxiliar.getTime() + minutosAux);
					 
					e.setFechaLlegada(auxiliar2);
					e.setEstado("despachado");
					
				//hbtDAO.guardar(EnvioToEntity(e));
				}
				v.setEstado("En uso");
				viaje.setEnvios(en);
				hbtDAO.modificar(VehiculoToEntity(v));
				hbtDAO.guardar(ViajeToEntity(viaje));
				}
				}
			}
			
			}
			System.out.println("s"+sOrigen.getIdSucursal());
			}
			
		}
		
	}
   
   //Agregue cosas para que trate de enviar   los pedidos prioritarios aunque no llegue al 70%
private List<PedidoDTO> ordenarPedidosPorPrioridad(List<PedidoDTO>pedidos) {
		
	List<PedidoDTO> aux=new ArrayList<PedidoDTO>();
	for(PedidoDTO p:pedidos)
	{
		 
			if(!p.getCargas().get(0).isDespachado())
			{
				aux.add(p);
			}
		
	}
	pedidos=new ArrayList<PedidoDTO>();
	pedidos=aux;
	PedidoDTO pedAux=new PedidoDTO();
	 for(int i=0;i<pedidos.size();i++)
	 {
    /*
    SucursalDTO o =obtenerSucursal(pedidos.get(i).getSucursalOrigen());
SucursalDTO d =obtenerSucursal(pedidos.get(i).getSucursalDestino());
ViajeDTO viajeDTO=calcularMejorFechaLlegada(o,d);
respuesta=false;
if(!pedidos.get(j).getFechaMaxima().before(viajeDTO.getFechaLlegada()))
{
	respuesta=enviarUrgente(pedidos.get(j));
   if(respuesta)
      {
      pedidos.remove(i);
      }
}
else if(!respuesta)
{ 
      */
		 for(int j=0;j<pedidos.size();j++)
		 {
			 if(pedidos.get(j).getFechaMaxima().before(pedidos.get(i).getFechaMaxima()))
			 {
				/*   pedAux=pedidos.get(i);
					pedAux2=pedidos.get(j);
                                  pedidos.remove(j);
                                  pedidos.remove(i);
                                 pedidos.add(i,pedAux2);
				 pedidos.add(j,pedAux);
				 */
				 
			 }
			 
		 }
       //]
	 }
	 return pedidos;
 
}
	public PedidoDTO obtenerPedido(int idPedido) throws RemoteException {
		return hbtDAO.obtenerPedido(idPedido);
	}

	public List<HabilitadoDTO> obtenerHabilitados() throws RemoteException {
		return hbtDAO.obtenerHabilitados();
	}

	public List<EnvioDTO> obtenerEnvios(String nombre) throws RemoteException {
		// TODO Auto-generated method stub
		System.out.println("RemoteObject");
		return hbtDAO.obtenerEnvios(nombre);
	}


public void recibir(ViajeDTO v)
{
	v=hbtDAO.obtenerViaje(v.getIdViaje());
	for(EnvioDTO e:v.getEnvios())
	{
		if(Integer.parseInt(e.getPedido().getSucursalDestino())==v.getSucursalDestino().getIdSucursal())
		{
			for(CargaDTO c:e.getPedido().getCargas())
			{
				c.setDespachado(true);
				hbtDAO.guardar(CargaToEntity(c));
			}
			 
		}
		else
			e.setEstado("listo");
			v.setFinalizado(true);
	}
	VehiculoDTO veh=hbtDAO.obtenerVehiculo(v.getVehiculo().getIdVehiculo());
	veh.setEstado("En uso");
	
	hbtDAO.modificar(ViajeToEntity(v));
	hbtDAO.modificar(VehiculoToEntity(veh));
	 
		 
}
	public List<TransporteDTO> obtenerTransportes() throws RemoteException {
		return hbtDAO.obtenerTransportes();
	}
	
	private SucursalDTO proxDestino(PedidoDTO pedido) {
		MapaDeRutaDTO mp = mapadeRuta;
		cargarMapaDeRuta();
		SucursalDTO s=obtenerSucursal(pedido.getSucursalOrigen());
		SucursalDTO sd=obtenerSucursal(pedido.getSucursalDestino());
		RutaDTO r=obtenerMejorRuta(s, sd);
		return r.getTrayectos().get(0).getSucursalDestino();
	}
		
	 public ViajeDTO calcularMejorFechaLlegada(SucursalDTO o,SucursalDTO d)
	 {
		 MapaDeRutaDTO mp = mapadeRuta;
			cargarMapaDeRuta();
			RutaDTO r=obtenerMejorRuta(o, d);
			float tiempo=0;
	for(TrayectoDTO t:r.getTrayectos())
	{
		tiempo=t.getTiempo()+tiempo;
	}
	long m=(long) tiempo;
	long milisegundos=60000;
	Date auxiliar= Calendar.getInstance().getTime();
	long minutosAux=m*milisegundos;
	Date auxiliar2=new Date(auxiliar.getTime() + minutosAux);
	ViajeDTO v=new ViajeDTO();
	v.setFechaLlegada(auxiliar2);
	return v;
	 }

	 private SucursalDTO obtenerSucursal(String nombre)
		{
			
			List<SucursalDTO> suc =hbtDAO.obtenerSucursales();
			for(SucursalDTO s:suc)
			{
				int i=Integer.parseInt(nombre);
				if(s.getIdSucursal()==i)
				{
					return s;
				}
			}
			return null;
		}
		 
	private List<VehiculoDTO> obtenerVehiculosListos()
	{
		List<VehiculoDTO> aux=new ArrayList<VehiculoDTO>();
		List<VehiculoDTO> vehiculos=hbtDAO.obtenerVehiculos();
		for(VehiculoDTO v:vehiculos)
		{
			if(v.getEstado().equals("En Deposito"))
				aux.add(v);
				
		}
		return aux;
	}

 
		public ClienteDTO obtenerClientePorID(int id)
		{
			return hbtDAO.obtenerClientePorID(id);
			
		}


		@Override
		public void cargarDatosIniciales() throws RemoteException {
		
			entities.Empresa e=new Empresa();
			e.setNombre("Empresa 1");
			e.setCUIT(234234);
			e.setDetallePoliticas("Detalle Política 1");
			e.setTipo("Tipo 1");
			e.setSaldoCuentaCorriente(15000);
			hbtDAO.guardar(e);
			
			entities.Particular p=new Particular();
			p.setNombre("Elio");
			p.setApellido("Mollo");
			p.setDNI(9418723);
			hbtDAO.guardar(p);
			
			entities.Direccion dO=new entities.Direccion();
			dO.setCalle("Calle 16");
			dO.setCP("1884");
			dO.setDepartamento("F");
			dO.setNumero(5403);
			dO.setPiso(2);
			hbtDAO.guardar(dO);
			
			entities.Direccion dD=new entities.Direccion();
			dD.setCalle("Calle 16");
			dD.setCP("1884");
			dD.setDepartamento("F");
			dD.setNumero(5403);
			dD.setPiso(2);
			hbtDAO.guardar(dD);
			
			entities.Pedido pedido=new entities.Pedido();
			pedido.setCliente(p);
			pedido.setDireccionCarga(dO);
			pedido.setDireccionDestino(dD);
			Date hoy=new Date();
			pedido.setFechaCarga(hoy);
			Date fmax=new Date(hoy.getTime() + (1000 * 60 * 60 * 24));
			pedido.setFechaMaxima(fmax);
			pedido.setHoraInicio(12);
			pedido.setHoraFin(13);
			pedido.setPrecio(1500);
			pedido.setSucursalOrigen("Berazategui");
			pedido.setSucursalDestino("Quilmes");
			pedido.setSolicitaAvionetaParticular(false);
			pedido.setSolicitaTransporteDirecto(false);
			ArrayList<entities.Carga> cargas=new ArrayList<entities.Carga>();
			entities.Carga carga=new entities.Carga();
			carga.setAlto(2);
			carga.setAncho(3);
			carga.setApilable(4);
			carga.setCondiciones("Apilable hasta 2 Pallets");
			carga.setDespachado(false);
			carga.setFragilidad("Normal");
			carga.setMercaderia("Electrónico");
			carga.setPeso(20);
			carga.setProfundidad(2);
			carga.setRefrigerable(false);
			carga.setTratamiento("Normal");
			carga.setVolumen(carga.getAlto()*carga.getAncho()*carga.getProfundidad());
			hbtDAO.guardar(carga);
			cargas.add(carga);
			entities.Carga carga2=new entities.Carga();
			carga2.setAlto(3);
			carga2.setAncho(6);
			carga2.setApilable(2);
			carga2.setCondiciones("No se recomienda Apilar");
			carga2.setDespachado(false);
			carga2.setFragilidad("Normal");
			carga2.setMercaderia("Electrónico");
			carga2.setPeso(80);
			carga2.setProfundidad(6);
			carga2.setRefrigerable(false);
			carga2.setTratamiento("Normal");
			carga2.setVolumen(carga2.getAlto()*carga2.getAncho()*carga2.getProfundidad());
			cargas.add(carga2);
			hbtDAO.guardar(carga2);
			pedido.setCargas(cargas);
			hbtDAO.guardar(pedido);
		}


		@Override
		public void altaClienteEmpresa(EmpresaDTO empresaDto) throws RemoteException {
			// TODO Auto-generated method stub
			Empresa e=new Empresa();
			//e=EmpresaToEntity(empresaDto);
			e.setCUIT(empresaDto.getCUIT());
			e.setDetallePoliticas(empresaDto.getDetallePoliticas());
			e.setNombre(empresaDto.getNombre());
			//e.setProductos(productos);
			e.setSaldoCuentaCorriente(empresaDto.getSaldoCuentaCorriente());
			e.setTipo(empresaDto.getTipo());
			hbtDAO.guardar(e);
		}


		@Override
		public void altaClienteParticular(ParticularDTO particularDto) throws RemoteException {
			// TODO Auto-generated method stub
			Particular c=new Particular();
			c.setApellido(particularDto.getApellido());
			c.setDNI(particularDto.getDNI());
			c.setNombre(particularDto.getNombre());
			hbtDAO.guardar(c);
		}


		@Override
		public void updateClienteEmpresa(EmpresaDTO empresaDto) throws RemoteException {
			// TODO Auto-generated method stub
			Empresa e=new Empresa();
			e.setIdCliente(empresaDto.getIdCliente());
			e.setCUIT(empresaDto.getCUIT());
			e.setDetallePoliticas(empresaDto.getDetallePoliticas());
			e.setNombre(empresaDto.getNombre());
			e.setSaldoCuentaCorriente(empresaDto.getSaldoCuentaCorriente());
			e.setTipo(empresaDto.getTipo());
			hbtDAO.modificar(e);
		}
		public void updateClienteParticular(ParticularDTO particularDto) throws RemoteException {
			// TODO Auto-generated method stub
			Particular p=new Particular();
			p.setIdCliente(particularDto.getIdCliente());
			p.setApellido(particularDto.getApellido());
			p.setDNI(particularDto.getDNI());
			p.setNombre(particularDto.getNombre());
			hbtDAO.modificar(p);
		}
		public void deleteClienteEmpresa(int idCliente) throws RemoteException {
			Empresa e=new Empresa();
			e.setIdCliente(idCliente);
			hbtDAO.borrar(e);
		}
		public void deleteClienteParticular(int idCliente) throws RemoteException {
			Particular p=new Particular();
			p.setIdCliente(idCliente);
			hbtDAO.borrar(p);
			
		}


		@Override
		public List<CargaDTO> listarCargas() throws RemoteException {
			return hbtDAO.listarCargas();
		}
}
