package com.cloud.survey.service;

import com.cloud.survey.dto.AnswerDTO;
import com.cloud.survey.dto.UserDTO;
import com.cloud.survey.entity.Answer;
import com.cloud.survey.entity.Question;
import com.cloud.survey.openfeign.AnalysisServiceClient;
import com.cloud.survey.openfeign.AuthServiceClient;
import com.cloud.survey.repository.AnswerRepository;
import com.cloud.survey.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Log4j2
@Service
@RequiredArgsConstructor
public class AnswerServiceImpl implements AnswerService {
    @Autowired
    private final AnswerRepository answerRepository;
    @Autowired
    private final QuestionRepository questionRepository;
    @Autowired
    private final AnalysisServiceClient analysisServiceClient;
    @Autowired
    private final AuthServiceClient authServiceClient;

    public List<Map<String,Object>> getUserAnswer (String userId, int surId){
        return answerRepository.findByRegIdAndSurId(userId, surId);
    }

    @Override
    public Map<String, Object> getSurveyAnswerAnalysis(int surId) {

        // 설문분석 통계옵션 조회
        List<Object> surveyAnalysisOptionList = analysisServiceClient.getTypeSubjectList(surId);

        // 설문 답변자, 답변 응답 시간대 조회
        List<Map<String,Object>> surveyAnalysisData = answerRepository.findBySurId(surId);

        //설문 답변자 상세정보 리스트
        List<UserDTO> answerUserList = authServiceClient.getUserDetailInfoList(surveyAnalysisData);

        Map<String,Object> map = new HashMap<>();
        map.put("answer_data_list", surveyAnalysisData);
        map.put("answer_user_list", answerUserList);
        return map;
    }

    @Override
    public void insertAnswer(Integer surId, List<AnswerDTO> answerDTOList) {
        List<Map<String, Object>> byRegIdAndSurId = answerRepository.findByRegIdAndSurId(answerDTOList.get(0).getRegId(), surId);
        if (byRegIdAndSurId == null) {
            answerDTOList.forEach(answerDTO -> {
                Optional<Question> byId = questionRepository.findById(answerDTO.getQueId());
                if (byId.isPresent()) {
                    Question question = byId.get();
                    Answer save = answerRepository.save(dtoToEntity(answerDTO, question));
                    if (save == null) {
                        log.error("답변 저장 실패");
                    }
                } else{
                    log.error("질문 조회 실패");
                }
            });
        } else {
            log.error("이미 설문에 참여한 사용자");
        }
        
    }

    @Override
    public void updateAnswer(Integer surId, List<AnswerDTO> answerDTOList) {
        answerDTOList.forEach(answerDTO -> {
            Optional<Answer> byId = answerRepository.findById(answerDTO.getAnsId());
            if (byId.isPresent()) {
                Answer answer = byId.get();
                answer.changeContent(answerDTO.getContent());
                answer.changeModId(answerDTO.getRegId());
                Answer save = answerRepository.save(answer);
            } else {
                log.error("findById 오류로 답변 변경 실패");
            }
        });
    }

    @Override
    @Transactional
    public void deleteAnswer(Integer ansId) {
        Optional<Answer> byId = answerRepository.findById(ansId);
        if (byId.isPresent()) {
            answerRepository.updateDeleteYn(ansId);
        } else {
            log.error("findById 오류로 답변 삭제 실패");
        }
    }
}
