package com.spotlight.falcon_ui.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.widget.LinearLayoutCompat
import com.spotlight.falcon_ui.R
import com.spotlight.falcon_ui.databinding.UiFalconCountryCircleViewBinding
import java.util.Locale

class FalconCountryCircleView(var mContext: Context, val mAttr: AttributeSet?): LinearLayoutCompat(mContext, mAttr) {
    private lateinit var binding: UiFalconCountryCircleViewBinding

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.ui_falcon_country_circle_view, this, true)
        binding = UiFalconCountryCircleViewBinding.bind(view)
    }

    fun initIconRes(countryCode: String) {
        binding.uiIcon.setImageResource(uiCode(countryCode))
    }

    fun uiCode(code: String): Int {
        return when(code.toLowerCase(Locale.getDefault())) {
            "ad" -> R.mipmap.ad
            "ae" -> R.mipmap.ae
            "al" -> R.mipmap.al
            "am" -> R.mipmap.am
            "ar" -> R.mipmap.ar
            "at" -> R.mipmap.at
            "au" -> R.mipmap.au
            "aw" -> R.mipmap.aw
            "ba" -> R.mipmap.ba
            "bd" -> R.mipmap.bd
            "be" -> R.mipmap.be
            "bg" -> R.mipmap.bg
            "bn" -> R.mipmap.bn
            "br" -> R.mipmap.br
            "bs" -> R.mipmap.bs
            "bt" -> R.mipmap.bt
            "by" -> R.mipmap.by
            "ca" -> R.mipmap.ca
            "ch" -> R.mipmap.ch
            "cl" -> R.mipmap.cl
            "cn" -> R.mipmap.cn
            "co" -> R.mipmap.co
            "cr" -> R.mipmap.cr
            "cy" -> R.mipmap.cy
            "cz" -> R.mipmap.cz
            "de" -> R.mipmap.de
            "dk" -> R.mipmap.dk
            "dz" -> R.mipmap.dz
            "ec" -> R.mipmap.ec
            "ee" -> R.mipmap.ee
            "eg" -> R.mipmap.eg
            "es" -> R.mipmap.es
            "fl" -> R.mipmap.fl
            "fr" -> R.mipmap.fr
            "gb" -> R.mipmap.gb
            "ge" -> R.mipmap.ge
            "gr" -> R.mipmap.gr
            "gt" -> R.mipmap.gt
            "hk" -> R.mipmap.hk
            "hr" -> R.mipmap.hr
            "hu" -> R.mipmap.hu
            "id" -> R.mipmap.id
            "ie" -> R.mipmap.ie
            "il" -> R.mipmap.il
            "im" -> R.mipmap.im
            "in" -> R.mipmap.inpng
            "ir" -> R.mipmap.ir
            "is" -> R.mipmap.ispng
            "it" -> R.mipmap.it
            "je" -> R.mipmap.je
            "jp" -> R.mipmap.jp
            "ke" -> R.mipmap.ke
            "kg" -> R.mipmap.kg
            "kh" -> R.mipmap.kh
            "kr" -> R.mipmap.kr
            "kz" -> R.mipmap.kz
            "la" -> R.mipmap.la
            "li" -> R.mipmap.li
            "lk" -> R.mipmap.lk
            "lt" -> R.mipmap.lt
            "lu" -> R.mipmap.lu
            "lv" -> R.mipmap.lv
            "mc" -> R.mipmap.mc
            "md" -> R.mipmap.md
            "me" -> R.mipmap.me
            "mk" -> R.mipmap.mk
            "mm" -> R.mipmap.mm
            "mn" -> R.mipmap.mn
            "mo" -> R.mipmap.mo
            "mt" -> R.mipmap.mt
            "my" -> R.mipmap.my
            "nl" -> R.mipmap.nl
            "no" -> R.mipmap.no
            "np" -> R.mipmap.np
            "nz" -> R.mipmap.nz
            "pa" -> R.mipmap.pa
            "pe" -> R.mipmap.pe
            "ph" -> R.mipmap.ph
            "pk" -> R.mipmap.pk
            "pl" -> R.mipmap.pl
            "pt" -> R.mipmap.pt
            "ro" -> R.mipmap.ro
            "rs" -> R.mipmap.rs
            "ru" -> R.mipmap.ru
            "se" -> R.mipmap.se
            "sg" -> R.mipmap.sg
            "si" -> R.mipmap.si
            "sk" -> R.mipmap.sk
            "th" -> R.mipmap.th
            "tm" -> R.mipmap.tm
            "tr" -> R.mipmap.tr
            "ua" -> R.mipmap.ua
            "us" -> R.mipmap.us
            "uy" -> R.mipmap.uy
            "uz" -> R.mipmap.uz
            "ve" -> R.mipmap.ve
            "vn" -> R.mipmap.vn
            "ye" -> R.mipmap.ye
            "za" -> R.mipmap.za
            "fast" -> R.mipmap.disposition_best_node
            "game" -> R.mipmap.disposition_game
            else -> R.mipmap.disposition_best_node
        }
    }
}