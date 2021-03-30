package com.dsceksu.myelper.Models

class UsersModel(var uid:String="",
                 val fullname:String ="",
                 val username:String ="",
                 val email:String ="",
                 val avatar:String ="",
                 val reg_date:Any ="",
                 val presence:String ="",
                 val phone_number:String ="",
                 val address:String ="",
                 val city:String ="",
                 val state:String ="",
                 val wallet_balance:String ="",
                 val campus:String = "",
                 val activated:Boolean? = null,
                 val ads_slot_left:Any? = null,
                 val banned:Boolean? = null,
val suspended:Boolean? = null) {
}
