package com.burak.yemektarifi2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.burak.yemektarifi2.databinding.FragmentTarifBinding
import com.google.android.material.snackbar.Snackbar


class tarifFragment : Fragment() {
    private var secilenGorsel :Uri?=null
    private var secilenBitmap : Bitmap?= null
    private var _binding: FragmentTarifBinding? = null
    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!
    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()
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
                binding.KaydetButton.isEnabled = true
                binding.SilButton.isEnabled =false
            }
            else{
                //eski bilgiymiş
                binding.KaydetButton.isEnabled =true
                binding.SilButton.isEnabled = false
            }

        }
    }

    fun kaydet(view: View){

    }
    fun sil(view: View){

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}