package co.edu.unicauca.question.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** OpcionRespuesta (UML): identificador visible (A, B, C, D) y texto. */
@Embeddable
public class QuestionOption {

    @Column(name = "option_id", length = 10)
    private String optionId;

    @Column(name = "option_text", length = 1000)
    private String text;

    protected QuestionOption() {
    }

    public QuestionOption(String optionId, String text) {
        this.optionId = optionId;
        this.text = text;
    }

    public String getOptionId() { return optionId; }
    public String getText() { return text; }
}
