# Smart Bookmark API

> 개인화된 지식 관리를 위한 '트리 구조 및 태그 기반' 북마크 API 프로젝트입니다.

이 프로젝트는 흩어져 있는 개인의 디지털 정보를 체계적으로 수집하고, 계층적 폴더 구조와 유연한 태깅 시스템을 통해 다각도로 정보를 분류하며, 궁극적으로는 지식 간의 연결성을 발견하고 활용할 수 있도록 돕는 개인화된 지식 관리(Personal Knowledge Management) 백엔드 시스템을 목표로 합니다.

<br>

## ✨ 주요 기능

* **계층형 폴더 관리**
  * 사용자가 원하는 대로 폴더를 생성, 조회, 수정, 삭제하며 계층적인 구조로 북마크를 정리할 수 있습니다.
  * 전체 폴더 구조를 한눈에 볼 수 있는 트리 조회 API를 제공합니다.
  * 폴더 삭제 시, 하위 폴더까지 함께 삭제하는 '강제 삭제' 옵션을 지원합니다.

* **북마크 CRUD 및 태그 시스템**
  * 북마크의 생성, 조회, 수정, 삭제 기능을 제공합니다.
  * 하나의 북마크에 여러 개의 태그를 자유롭게 할당하여 다각도로 콘텐츠를 분류할 수 있습니다.

* **지능형 검색**
  * 키워드(제목, 설명) 또는 하나 이상의 태그를 조합하여 북마크를 검색할 수 있습니다.
  * 검색 결과는 단순 조회가 아닌, 자체적인 점수 계산 로직을 통해 관련도 높은 순으로 정렬하여 제공합니다.
 
* **웹 프로토타입 (Thymeleaf)**
* 구현된 API를 활용하여 폴더 트리를 시각적으로 보여주고, 북마크를 검색할 수 있는 간단한 웹 UI를 제공합니다.

<br>

## 🛠️ 사용 기술

* **Language:** Java 17
* **Framework:** Spring Boot 3.3.1
* **View Template:** Thymeleaf
* **Data Access:** Spring Data JPA, Hibernate
* **Database:** H2 Database (개발용)
* **API Documentation:** Springdoc OpenAPI (Swagger UI)
* **Build Tool:** Gradle
* **Others:** Lombok, Validation

<br>

## 📖 API 문서

모든 API 엔드포인트에 대한 상세한 명세와 테스트 기능은 애플리케이션 실행 후 아래 링크에서 확인하실 수 있습니다.

* **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

<br>

## 🚀 로컬 환경에서 실행하기

#### 1. 사전 요구 사항
- JDK 17 또는 그 이상
- IntelliJ IDEA 또는 다른 Java IDE
- Git

#### 2. 프로젝트 클론 및 빌드
```bash
# 1. Git 저장소를 로컬 컴퓨터에 복제합니다.
git clone [이 프로젝트의 GitHub 저장소 URL]

# 2. 프로젝트 폴더로 이동합니다.
cd [프로젝트 폴더 이름]

# 3. Gradle을 사용하여 프로젝트를 빌드합니다. (권장)
./gradlew build
```

#### 3. 애플리케이션 실행
IntelliJ IDEA에서 프로젝트를 열고, 메인 애플리케이션 클래스(`ApiApplication.java`)를 직접 실행하거나, 터미널에서 다음 명령어를 입력하여 실행합니다.
```bash
./gradlew bootRun
```
애플리케이션은 `http://localhost:8080` 에서 실행됩니다.

<br>

## 🗃️ 데이터베이스 정보 (H2)

개발 환경에서는 H2 인메모리 데이터베이스를 사용합니다. 애플리케이션 실행 후 아래 주소로 접속하여 데이터베이스 상태를 직접 확인할 수 있습니다.

* **H2 Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
* **JDBC URL:** 애플리케이션 시작 로그를 확인하거나, `application.properties`/`.yml` 파일에 명시된 URL 사용. (예: `jdbc:h2:mem:testdb`)
* **Username:** `sa`
* **Password:** (비워둠)

---
*이 문서는 프로젝트 진행에 따라 계속 업데이트됩니다.*
