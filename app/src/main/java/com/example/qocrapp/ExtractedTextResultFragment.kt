package com.example.qocrapp

import android.graphics.Bitmap
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.qocrapp.databinding.ExtractedTextResultFragmentBinding
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage

class ExtractedTextResultFragment(
    private val bitmap: Bitmap,
    private val actionCallback: (String) -> Unit,
) : DialogFragment() {

    private var _binding: ExtractedTextResultFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = ExtractedTextResultFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageView.setImageBitmap(bitmap)

        binding.textView.movementMethod = ScrollingMovementMethod()

        val detector = FirebaseVision.getInstance()
            .onDeviceTextRecognizer

        val image = FirebaseVisionImage.fromBitmap(
            bitmap
        )

        binding.progressCircular.visibility = View.VISIBLE
        detector.processImage(image)
            .addOnSuccessListener { firebaseVisionText ->
                binding.progressCircular.visibility = View.GONE

                val resultText = firebaseVisionText.text.replace("\n", " ")

                binding.textView.text = resultText
                binding.layoutAction.visibility = View.VISIBLE
            }
            .addOnFailureListener { e ->
                binding.progressCircular.visibility = View.GONE

                binding.textView.text = e.localizedMessage
            }

        binding.btnSummarize.setOnClickListener {
            ResultFragment(
                extractedText = binding.textView.text.toString()
            ).show(parentFragmentManager, ResultFragment::class.simpleName)

            dismiss()
        }
    }
}