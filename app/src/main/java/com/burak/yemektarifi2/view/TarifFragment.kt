package com.burak.yemektarifi2.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.room.Room
import com.burak.yemektarifi2.databinding.FragmentTarifBinding
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import model.Tarif
import roomdb.TarifDAO
import roomdb.tarifDataBase
import java.io.ByteArrayOutputStream
import kotlin.math.max


class tarifFragment : Fragment() {
    private var secilenGorsel :Uri?=null
    private var secilenBitmap : Bitmap?= null
    private var _binding: FragmentTarifBinding? = null
    private lateinit var db : tarifDataBase
    private lateinit var TarifDao : TarifDAO
    private var mDisposable = CompositeDisposable()
    private var secilenTarif :Tarif? = null
    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!
    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()
        db = Room.databaseBuilder(requireContext(),tarifDataBase::class.java,"tarifler").build()
        TarifDao = db.TarifDAO()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTarifBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageView.setOnClickListener{gorselSec(it)}
        binding.KaydetButton.setOnClickListener { kaydet(it) }
        binding.SilButton.setOnClickListener { sil(it) }
        arguments?.let {
            val bilgi = tarifFragmentArgs.fromBundle(it).bilgi
            if(bilgi == "yeni"){
                //yeni bilgi gelmiş
                secilenTarif = null
                binding.KaydetButton.isEnabled = true
                binding.SilButton.isEnabled =false
                binding.isimText.setText("")
                binding.malzemeText.setText("")
            }
            else{
                //eski bilgiymiş
                binding.KaydetButton.isEnabled =true
                binding.SilButton.isEnabled = false
                val id = tarifFragmentArgs.fromBundle(it).id

                mDisposable.add(
                    TarifDao.findById(id).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(this::handleResponse)
                )


            }

        }
    }
    private fun handleResponse(tarif: Tarif){
        binding.isimText.setText(tarif.isim)
        binding.malzemeText.setText(tarif.malzeme)
        val bitmap = BitmapFactory.decodeByteArray(tarif.gorsel,0,tarif.gorsel.size)
        binding.imageView.setImageBitmap(bitmap)
        secilenTarif = tarif
    }
    fun kaydet(view: View){
        val isim = binding.isimText.toString()
        val malzeme = binding.malzemeText.toString()
        if(secilenBitmap != null){
            val kucukbitmap = kucukBitMapOlustur(secilenBitmap!!,300)
            val outputStream = ByteArrayOutputStream()
            kucukbitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream)
            val byteDizisi = outputStream.toByteArray()
            val tarif = Tarif(isim,malzeme,byteDizisi)

            //RxJava
            mDisposable.add(TarifDao.insert(tarif).subscribeOn(Schedulers.io()).
            observeOn(AndroidSchedulers.mainThread()).subscribe(this::handleResponseForInsert))

        }
    }

    private fun handleResponseForInsert(){
        //bir önceki fragmente dön başarılı toast mesajı yaz.
        val action = tarifFragmentDirections.actionTarifFragmentToListeFragment()
        Navigation.findNavController(requireView()).navigate(action)

    }

    fun sil(view: View){
        if(secilenTarif != null){
            mDisposable.add(
                TarifDao.delete(tarif = secilenTarif!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseForInsert)
            )
        }
    }
    fun gorselSec(view: View){

        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU){
            //Yeni telefona
            if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)
            {
                //izin verilmemiş izin isteyeceğiz.
                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_MEDIA_IMAGES)){
                    Snackbar.make(view,"BU izin olmalı foto laızm!!",Snackbar.LENGTH_INDEFINITE).setAction(
                        "izin ver", {
                            //izin isticem
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }
                    ).show()
                }
                else
                {
                    //izin isticez
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }
            else{
                //izin verilmiş galeriye gidicez.
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }
        else
        {//ESKİ TELLERE
            if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                //izin verilmemiş izin isteyeceğiz.
                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_EXTERNAL_STORAGE)){
                    Snackbar.make(view,"BU izin olmalı foto laızm!!",Snackbar.LENGTH_INDEFINITE).setAction(
                        "izin ver", {
                            //izin isticem
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    ).show()
                }
                else
                {
                    //izin isticez
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
            else{
                //izin verilmiş galeriye gidicez.
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }

    }

    fun registerLauncher(){
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
        if(result.resultCode == AppCompatActivity.RESULT_OK){
            val intentFromResult = result.data
            if (intentFromResult != null){
                secilenGorsel = intentFromResult.data
                //yeni telefonlarda::
                if (Build.VERSION.SDK_INT >= 28){
                    val source = ImageDecoder.createSource(requireActivity().contentResolver,secilenGorsel!!)
                    secilenBitmap = ImageDecoder.decodeBitmap(source)
                    binding.imageView.setImageBitmap(secilenBitmap)

                }
                else{
                    //eski telefonlarda:
                    secilenBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,secilenGorsel)
                    binding.imageView.setImageBitmap(secilenBitmap)
                }


            }
        }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result->
            if(result){
                //izin verilmiş galeriye git.
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
            else{
                //izin verilmemiş.
                Toast.makeText(requireContext(),"ayarlardan izin verin lütfen!!",Toast.LENGTH_LONG).show()
            }

        }

    }


    fun kucukBitMapOlustur(kullanicininSectigiBitMap :Bitmap , maximumBoyut :Int) : Bitmap{
        var width = kullanicininSectigiBitMap.width
        var height = kullanicininSectigiBitMap.height
        var oran : Double = width.toDouble()/height.toDouble()
        if(oran > 1){
            //görsel yatay.
            width =maximumBoyut
            val kisaltilmisYükseklik = width/oran
            height = kisaltilmisYükseklik.toInt()
        }
        else{
            //görsel dikey.
            height = maximumBoyut
            val kisaltilmisEn = height/oran
            width = kisaltilmisEn.toInt()
        }

        return Bitmap.createScaledBitmap(kullanicininSectigiBitMap,width,height,true)

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}