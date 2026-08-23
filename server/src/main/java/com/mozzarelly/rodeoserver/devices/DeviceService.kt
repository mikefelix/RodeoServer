package com.mozzarelly.rodeoserver.devices

/*class DeviceService @Inject constructor(
  private val deviceDao: DeviceDao,
  private val etek: EtekDeviceRepository,
  private val workQueue: WorkQueue,
  private val db: AppDatabase
){
  val scope = CoroutineScope(SupervisorJob())
  val devices: StateFlow<List<Device>> = deviceDao.getAllDevices()
    .stateIn(scope, SharingStarted.Eagerly, emptyList())

  suspend fun updateDevice(name: String, isOn: Boolean, lock: Boolean) {
    updateDevice(deviceDao.get(name).copy(locked = lock, isOn = isOn))
  }

  suspend fun updateDevice(device: Device) {
    val handler = when (device.subsystem) {
      Subsystem.Etek -> etek
      Subsystem.Tuya -> TODO()
      Subsystem.Tasmota -> TODO()
      Subsystem.Wemo -> TODO()
      Subsystem.Shelly -> TODO()
    }

    db.withTransaction {
      handler.updateLocal(device)
      workQueue.enqueue(getUpdateWork(device))
      handler.update(device)
    }
  }
}
*/
