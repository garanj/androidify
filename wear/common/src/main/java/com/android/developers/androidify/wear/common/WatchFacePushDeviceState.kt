package com.android.developers.androidify.wear.common

//@Serializable
//data class WatchFacePushDeviceState(
//    val isReadyForTransfer: Boolean = false,
//    val hasActiveWatchFace: Boolean = false,
//    val hasGrantedSetActivePermission: Boolean = false,
//    val canRequestSetActivePermission: Boolean = true,
//    val hasUsedSetActiveApi: Boolean = false,
//)
//
//@OptIn(ExperimentalSerializationApi::class)
//object WatchFacePushDeviceStateSerializer : Serializer<WatchFacePushDeviceState> {
//    override val defaultValue = WatchFacePushDeviceState()
//
//    override suspend fun readFrom(input: InputStream): WatchFacePushDeviceState {
//        try {
//            return ProtoBuf.decodeFromByteArray(input.readBytes())
//        } catch (serialization: SerializationException) {
//            throw CorruptionException("Unable to read state", serialization)
//        }
//    }
//
//    override suspend fun writeTo(t: WatchFacePushDeviceState, output: OutputStream) {
//        output.write(
//            ProtoBuf.encodeToByteArray(t),
//        )
//    }
//}