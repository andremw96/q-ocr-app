package com.example.qocrapp

import android.graphics.Bitmap
import android.media.Image
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.example.qocrapp.databinding.ExtractedTextResultFragmentBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

enum class ActionGenerate(
    val stringResource: Int,
) {
    STUDY_NOTE(R.string.study_note_action),
    SUMMARY(R.string.summary_action),
    KEYWORDS(R.string.keywords_action),
    OUTLINE(R.string.outline_action),
    TRANSLATE(R.string.translate_action),
    SOLVE(R.string.solve_action),
    FREE_TEXT(-1),
    UNKNOWN(R.string.unknown)
}

class ExtractedTextResultFragment(
    private val bitmap: Bitmap,
    private val image: Image,
    private val imageRotationDegrees: Int,
) : DialogFragment() {

    private var _binding: ExtractedTextResultFragmentBinding? = null
    private val binding get() = _binding!!

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

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

        val image = InputImage.fromMediaImage(
            image,
            imageRotationDegrees
        )

        binding.progressCircular.visibility = View.VISIBLE
        binding.btnGenerate.isEnabled = false
        recognizer.process(image)
            .addOnSuccessListener { firebaseVisionText ->
                binding.progressCircular.visibility = View.GONE
                binding.btnGenerate.isEnabled = true

                val stringBuilder = StringBuilder()
                for (block in firebaseVisionText.textBlocks) {
                    stringBuilder.append(block.text)
                    stringBuilder.append("\n\n")
                }
                if (stringBuilder.isEmpty()) {
                    binding.textView.setText("text not found")
                } else {
                    binding.textView.setText(stringBuilder)
                    binding.layoutAction.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                binding.progressCircular.visibility = View.GONE

                binding.textView.setText(e.localizedMessage)
            }

        binding.chipGroupAction.setOnCheckedStateChangeListener { group, checkedIds ->
            binding.editTextFreeText.isVisible = checkedIds.contains(R.id.chip_translate) || checkedIds.contains(R.id.chip_free_text)
            if (checkedIds.contains(R.id.chip_translate)) {
                binding.editTextFreeText.hint = getString(R.string.translate_into)
            } else if (checkedIds.contains(R.id.chip_free_text)) {
                binding.editTextFreeText.hint = getString(R.string.insert_any_command)
            }
        }

        binding.btnGenerate.setOnClickListener {
            val checkedAction = checkedChipToActionGenerate(binding.chipGroupAction.checkedChipId)
            val freeText = binding.editTextFreeText.text.toString()
            val isReprocessChecked = binding.chkboxReprocess.isChecked

            ResultFragment(
                extractedText = binding.textView.text.toString(),
                freeText = freeText,
                actionGenerate = checkedAction,
                isReprocessChecked = isReprocessChecked,
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
            R.id.chip_free_text -> ActionGenerate.FREE_TEXT
            else -> ActionGenerate.UNKNOWN
        }
    }
}
