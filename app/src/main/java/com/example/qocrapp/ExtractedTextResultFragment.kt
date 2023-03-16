package com.example.qocrapp

import android.graphics.Bitmap
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.example.qocrapp.databinding.ExtractedTextResultFragmentBinding
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage

enum class ActionGenerate(
    val stringResource: Int,
) {
    STUDY_NOTE(R.string.study_note_action),
    SUMMARY(R.string.summary_action),
    KEYWORDS(R.string.keywords_action),
    OUTLINE(R.string.outline_action),
    TRANSLATE(R.string.translate_action),
    SOLVE(R.string.solve_action),
    UNKNOWN(R.string.unknown)
}

class ExtractedTextResultFragment(
    private val bitmap: Bitmap,
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
        binding.btnGenerate.isEnabled = false
        detector.processImage(image)
            .addOnSuccessListener { firebaseVisionText ->
                binding.progressCircular.visibility = View.GONE
                binding.btnGenerate.isEnabled = true

                val resultText = firebaseVisionText.text.replace("\n", " ")

                binding.textView.setText(resultText)
                binding.layoutAction.visibility = View.VISIBLE
            }
            .addOnFailureListener { e ->
                binding.progressCircular.visibility = View.GONE

                binding.textView.setText(e.localizedMessage)
            }

        binding.chipGroupAction.setOnCheckedStateChangeListener { group, checkedIds ->
            binding.editTranslateInto.isVisible = checkedIds.contains(R.id.chip_translate)
        }

        binding.btnGenerate.setOnClickListener {
            val checkedAction = checkedChipToActionGenerate(binding.chipGroupAction.checkedChipId)
            val translateIntoText = binding.editTranslateInto.text.toString()

            ResultFragment(
                extractedText = binding.textView.text.toString(),
                translateInto = translateIntoText,
                actionGenerate = checkedAction
            ).show(parentFragmentManager, ResultFragment::class.simpleName)

            dismiss()
        }
    }

    private fun checkedChipToActionGenerate(checkedChipId: Int): ActionGenerate {
        return when (checkedChipId) {
            R.id.chip_study_note -> ActionGenerate.STUDY_NOTE
            R.id.chip_summary -> ActionGenerate.SUMMARY
            R.id.chip_keywords -> ActionGenerate.KEYWORDS
            R.id.chip_outline -> ActionGenerate.OUTLINE
            R.id.chip_translate -> ActionGenerate.TRANSLATE
            R.id.chip_solve -> ActionGenerate.SOLVE
            else -> ActionGenerate.UNKNOWN
        }
    }
}
