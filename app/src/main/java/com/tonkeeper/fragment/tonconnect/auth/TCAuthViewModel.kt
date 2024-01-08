package com.tonkeeper.fragment.tonconnect.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonkeeper.App
import com.tonkeeper.api.TonNetwork
import com.tonkeeper.core.tonconnect.AppRepository
import com.tonkeeper.core.tonconnect.Bridge
import com.tonkeeper.core.tonconnect.Proof
import com.tonkeeper.core.tonconnect.TCManifestRepository
import com.tonkeeper.core.tonconnect.models.TCData
import com.tonkeeper.core.tonconnect.models.TCItem
import com.tonkeeper.core.tonconnect.models.TCRequest
import com.tonkeeper.core.tonconnect.models.reply.TCAddressItemReply
import com.tonkeeper.core.tonconnect.models.reply.TCConnectEventSuccess
import com.tonkeeper.core.tonconnect.models.reply.TCReply
import io.ktor.util.hex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ton.api.pub.PublicKeyEd25519
import org.ton.block.AddrStd
import org.ton.block.StateInit
import org.ton.boc.BagOfCells
import org.ton.cell.CellBuilder
import org.ton.crypto.base64
import org.ton.tlb.storeTlb

class TCAuthViewModel: ViewModel() {


    private val _dataState = MutableStateFlow<TCData?>(null)
    val dataState = _dataState.asStateFlow()

    private val _connectState = MutableStateFlow(ConnectState.Default)
    val connectState = _connectState.asStateFlow()


    private val bridge = Bridge()
    private val manifestRepository = TCManifestRepository()
    private val appRepository = AppRepository()
    private val proof = Proof()

    fun requestData(request: TCRequest) {
        viewModelScope.launch {
            val wallet = App.walletManager.getWalletInfo() ?: return@launch

            val manifest = manifestRepository.manifest(request.payload.manifestUrl)

            val data = TCData(
                manifest = manifest,
                accountId = wallet.accountId.lowercase(),
                clientId = request.id,
                items = request.payload.items
            )

            _dataState.tryEmit(data)
        }
    }

    fun connect() {
        val data = _dataState.value ?: return
        viewModelScope.launch {
            try {
                sendConnect(data)
                _connectState.tryEmit(ConnectState.Success)
            } catch (e: Throwable) {
                _connectState.tryEmit(ConnectState.Error)
            }
        }
    }

    private suspend fun sendConnect(data: TCData) = withContext(Dispatchers.IO) {
        val wallet = App.walletManager.getWalletInfo() ?: throw Exception("Wallet not found")
        val accountId = wallet.accountId
        val app = appRepository.createApp(accountId, data.url, data.clientId)

        val items = mutableListOf<TCReply>()
        for (requestItem in data.items) {
            if (requestItem.name == TCItem.TON_ADDR) {
                items.add(createAddressItemReply(
                    accountId = accountId,
                    stateInit = wallet.stateInit,
                    publicKey = wallet.publicKey
                ))
            } else if (requestItem.name == TCItem.TON_PROOF) {
                items.add(proof.createProofItemReplySuccess(
                    requestItem.payload,
                    app.domain,
                    AddrStd.parse(accountId),
                    App.walletManager.getPrivateKey(wallet.id)
                ))
            }
        }

        val event = TCConnectEventSuccess(items)
        bridge.sendEvent(event.toJSON(), app)
    }

    private fun createAddressItemReply(
        accountId: String,
        stateInit: StateInit,
        publicKey: PublicKeyEd25519,
    ): TCAddressItemReply {
        val walletStateInit = getWalletStateInit(stateInit)

        return TCAddressItemReply(
            address = accountId,
            network = TonNetwork.MAINNET.value.toString(),
            walletStateInit = walletStateInit,
            publicKey = hex(publicKey.toByteArray())
        )
    }

    private fun getWalletStateInit(
        stateInit: StateInit
    ): String {
        val cell = CellBuilder()
            .storeTlb(StateInit.tlbCodec(), stateInit)
            .endCell()
        return base64(BagOfCells(cell).toByteArray())
    }
}