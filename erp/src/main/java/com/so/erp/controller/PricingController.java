package com.so.erp.controller;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.so.erp.model.Buyer;
import com.so.erp.model.Pricing;
import com.so.erp.model.Product;
import com.so.erp.service.BuyerService;
import com.so.erp.service.PricingService;
import com.so.erp.service.ProductService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
public class PricingController {

	@Autowired
	private PricingService prs;
	@Autowired
	private BuyerService bs;
	@Autowired
	private ProductService pds;
	
//	by현서. 판매가 리스트, 정렬순서
	@RequestMapping("pricing")
	public String pricing(Model model, Pricing pricing) {
		// 기본 패이지당 20행 출력
		int rowPerPage = 20 ;
		// n개 보기 체크박스 선택 시 해당 값이 넘어옴
		if (pricing.getRowPerPage() != 0) {
			rowPerPage = pricing.getRowPerPage();
		} 
		// 페이지 번호 미선택 시 1페이지 고정
		if (pricing.getPageNum() == null || pricing.getPageNum().equals("")) {
			pricing.setPageNum("1");
		}
		// 리스트 정렬 초기화(기본 등록순)
		pricing.setDel("N");		
		pricing.setSortBuyerCd(0);
		pricing.setSortBname(0);
		pricing.setSortProductCd(0);
		pricing.setSortPname(0);
		pricing.setSortPrice(0);
		pricing.setSortStartdate(0);
		pricing.setSortEnddate(0);
		pricing.setSortDiscountrate(0);
		pricing.setSortFinalPrice(0);
		pricing.setSortAdddate(1);
		pricing.setSortStatusdate(0);

		int currentPage = Integer.parseInt(pricing.getPageNum());
		// 판매가의 모든 데이터 갯수 저장
		int total = prs.getTotal(pricing);
		pricing.pagingBean(currentPage, rowPerPage, total);
		
		// 해당 페이지의 첫 글 번호
		int startRow = (currentPage - 1) * rowPerPage + 1;
		// 해당 페이지의 마지막 글 번호
		int endRow = startRow + rowPerPage - 1;
		
		pricing.setStartRow(startRow);
		pricing.setEndRow(endRow);
		
		// pricingList 객체생성 및 데이터 가져옴
		List<Pricing> pricingList = new ArrayList<Pricing>();
		pricingList = prs.pricingList(pricing);
		
		
		// 판매가에 할인율을 계산하여 finalPrice에 저장
		for (Pricing pricing1 : pricingList) {
			pricing1.setFinalPrice(pricing1.getPrice() * 
					(1 - ((double)pricing1.getDiscountrate()/100)));
		}
		
		// 고객명과 상품명 데이터 조인해 가져옴
		List<Buyer> buyerList = bs.list();
		List<Product> productList = pds.allList(); 
		
		model.addAttribute("pricingList", pricingList);
		model.addAttribute("buyerList", buyerList);
		model.addAttribute("productList", productList);
		return "nolay/pricing";
	}
	
	
	//by현서. 검색
	@RequestMapping("pricingSearch")
	public String pricingSearch(Model model, String keyword, Pricing pricing) {
		
		try {
			// JSONParser 객체 선언
			JSONParser p = new JSONParser();
			// keyword로 넘어온 jason 문자열 구문 분석
			Object obj = p.parse(keyword);
			// 구문 분석된 개체를 jasonObject로 변환
			JSONObject keywordObj = JSONObject.fromObject(obj);
			
			
			// 변환된 값을 String 타입으로 변환 후 일치하는 변수명에 저장
			String buyerCd = (String) keywordObj.get("buyerCd");
			pricing.setBuyerCd(buyerCd);
			
			String productCd = (String) keywordObj.get("productCd");
			pricing.setProductCd(productCd);
			// int 타입은 String으로 먼저 변환 후 Integer.valueOf 으로 형변환
			int startPrice = Integer.valueOf((String)keywordObj.get("startPrice"));
			pricing.setStartPrice(startPrice);
		
			int endPrice = Integer.valueOf((String)keywordObj.get("endPrice"));
			pricing.setEndPrice(endPrice);
			// Date 타입은 값이 null이면 String으로 아니면 Date타입으로 저장
			String validDate = (String) keywordObj.get("validDate");
			if (validDate != null && !validDate.equals("") ) {
				Date date = Date.valueOf(validDate);
				pricing.setValidDate(date);
			}
			int discountrate = Integer.valueOf((String)keywordObj.get("discountrate"));
			pricing.setDiscountrate(discountrate);
			
			String currency = (String) keywordObj.get("currency");
			pricing.setCurrency(currency);
			
			String del = (String) keywordObj.get("del");
			pricing.setDel(del);
			
			
			// 검색 후 리스트 정렬 변경 시 검색조건 유지를 위해 정렬값도 같이 보냄 
			int sortBuyerCd = Integer.valueOf((String) keywordObj.get("sortBuyerCd"));
			pricing.setSortBuyerCd(sortBuyerCd);
			int sortBname = Integer.valueOf((String) keywordObj.get("sortBname"));
			pricing.setSortBname(sortBname);
			int sortProductCd = Integer.valueOf((String) keywordObj.get("sortProductCd"));
			pricing.setSortProductCd(sortProductCd);
			int sortPname = Integer.valueOf((String) keywordObj.get("sortPname"));
			pricing.setSortPname(sortPname);
			int sortPrice = Integer.valueOf((String) keywordObj.get("sortPrice"));
			pricing.setSortPrice(sortPrice);
			int sortStartdate = Integer.valueOf((String) keywordObj.get("sortStartdate"));
			pricing.setSortStartdate(sortStartdate);
			int sortEnddate = Integer.valueOf((String) keywordObj.get("sortEnddate"));
			pricing.setSortEnddate(sortEnddate);
			int sortDiscountrate = Integer.valueOf((String) keywordObj.get("sortDiscountrate"));
			pricing.setSortDiscountrate(sortDiscountrate);
			int sortFinalPrice = Integer.valueOf((String) keywordObj.get("sortFinalPrice"));
			pricing.setSortFinalPrice(sortFinalPrice);
			int sortCurrency = Integer.valueOf((String) keywordObj.get("sortCurrency"));
			pricing.setSortCurrency(sortCurrency);
			int sortAdddate = Integer.valueOf((String) keywordObj.get("sortAdddate"));
			pricing.setSortAdddate(sortAdddate);
			int sortStatusdate = Integer.valueOf((String) keywordObj.get("sortStatusdate"));
			pricing.setSortStatusdate(sortStatusdate);
			
			// 페이징 값 저장
			int rowPerPage = Integer.valueOf((String) keywordObj.get("rowPerPage"));
			int currentPage = Integer.valueOf((String) keywordObj.get("currentPage"));
			int total = prs.getTotal(pricing);
			
			pricing.pagingBean(currentPage, rowPerPage, total);
			
			int startRow = (currentPage - 1) * rowPerPage + 1;
			int endRow = startRow + rowPerPage - 1;
			pricing.setStartRow(startRow);
			pricing.setEndRow(endRow);
			
			
			// DB에서 데이터를 불러옴
			List<Pricing> searchList = prs.search(pricing);
			List<Buyer> buyerList = bs.ndlist();
			List<Product> productList = pds.allList();
			
			// 할인율을 적용한 최종가격 계산
			for (Pricing pricing1 : searchList) {
				pricing1.setFinalPrice(pricing1.getPrice() * (1 - ((double)pricing1.getDiscountrate()/100)));
			}
			
			model.addAttribute("pricingList", searchList);
			model.addAttribute("buyerList", buyerList);
			model.addAttribute("productList", productList);
			
		} catch (ParseException e) {
			System.out.println(e.getMessage());
		}
		return "nolay/pricing";
		
		 
	}
	
	
//	by현서. 삭제
	@RequestMapping("pricingDelete")
	@ResponseBody
	public int pricingDelete(@RequestParam(name="checkRows")String[] arr, Pricing pricing) throws java.text.ParseException{
		
		// result 초기화
		int result = 0;
			// 체크된 체크박스의 array를 분리하여 보냄
			for (String i : arr) {
				// 한 행의 데이터를 '&'를 기준으로 분리하여 String 배열에 저장
				String[] a = i.split("&");
				
				// 삭제 시 필요한 pk가 되는 각 컬럼의 인덱스번호로 추출하여 pricing 객체에 저장 
				String buyerCd = a[0];
				String productCd = a[1];
				Date startdate = Date.valueOf(a[2]);
				Date enddate = Date.valueOf(a[3]);
				
				pricing.setBuyerCd(buyerCd);
				pricing.setProductCd(productCd);
				pricing.setStartdate(startdate);
				pricing.setEnddate(enddate);

				// 삭제 메서드 실행
				prs.pricingDelete(pricing);
			}
		result = 1;
		return result;
	}
	
//	by.현서 삭제항목 복구
	@RequestMapping("pricingRestore")
	@ResponseBody
	public int pricingRestore(@RequestParam(name="checkRows")String[] arr, Pricing pricing) throws java.text.ParseException{
		
		// 삭제와 동일
		int result = 0;
			for (String i : arr) {
				String[] a = i.split("&");
				
				String buyerCd = a[0];
				String productCd = a[1];
				Date startdate = Date.valueOf(a[2]);
				Date enddate = Date.valueOf(a[3]);
				
				pricing.setBuyerCd(buyerCd);
				pricing.setProductCd(productCd);
				pricing.setStartdate(startdate);
				pricing.setEnddate(enddate);
				
				prs.pricingRestore(pricing);
			}
		result = 1;
		return result;
	}
	
	
//	by.현서 판매가 등록
	@RequestMapping("pricingInsert")
	@ResponseBody
	public boolean pricingInsert(Model model, @RequestParam(name="items")String items) throws ParseException {		
		
		// result 초기화
		boolean result = false;
		
		try {
			// jsp에서 보내온 json데이터를 jasonArray로 변환
			JSONParser p = new JSONParser();
			Object obj = p.parse(items);
			JSONArray arr = JSONArray.fromObject(obj);
			
			Pricing pricing = new Pricing(); 
			
			// 등록할 각 행의 데이터를 추출하여 형변환
			for (int i = 0; i < arr.size(); i++) {
				// i번째 행 추출
				JSONObject itemObj = (JSONObject) arr.get(i);
				// 각 타입에 맞는 변수명으로 저장
				String buyerCd = (String) itemObj.get("buyerCd");
				String productCd = (String) itemObj.get("productCd");
				String rdate = (String) itemObj.get("startdate");
				Date startdate = Date.valueOf(rdate);
				rdate = (String) itemObj.get("enddate");
				Date enddate = Date.valueOf(rdate);
				int price = Integer.parseInt((String) itemObj.get("price"));
				String currency = (String) itemObj.get("currency");
				int discountrate = Integer.parseInt((String) itemObj.get("discountrate"));
				
				pricing.setBuyerCd(buyerCd);
				pricing.setProductCd(productCd);
				pricing.setStartdate(startdate);
				pricing.setEnddate(enddate);
				pricing.setPrice(price);
				pricing.setCurrency(currency);
				pricing.setDiscountrate(discountrate);
				
				// insert메서드 실행
				prs.pricingInsert(pricing);
				result = true;
			}
		
		} catch (Exception e) {
			System.out.println(e.getMessage());
			result = false;
		}
		return result;
	}
	
	//by현서. 등록 시 중복 체크
	@RequestMapping("overlapCheck")
	@ResponseBody
	public int overlapCheck(Pricing pricing) {
		// result 초기화
		int result = 0;
		// pk에 해당하는 값을 count하여 1 이상이면 중복
		result = prs.overlapCheck(pricing);

		return result;
	}
	
	//by현서. 수정
	@RequestMapping("pricingUpdate")
	@ResponseBody
	public int pricingUpdate(Pricing pricing) {
		int result = 0;
		result = prs.pricingUpdate(pricing);
		return result;
	}
	
	// by해민. 
	@RequestMapping("getPrice")
	@ResponseBody
	public int getPrice(String buyerCd, String productCd, String orderdate) {
		
		Pricing pricing = new Pricing();
		pricing.setBuyerCd(buyerCd);
		pricing.setProductCd(productCd);
		
		Date date = Date.valueOf(orderdate);
		pricing.setValidDate(date);
		
		
		int price = 0;
		try {
			price = prs.getPrice(pricing);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			price = -123;
			
		}
		
		return price;
	}
	
	// by창률.
	@RequestMapping("getPricingProductList")
	@ResponseBody
	public String[] getPricingProductList(String buyerCd, @RequestParam(name="orderdate") String orderdate) {
		
		Date date = Date.valueOf(orderdate);
		Pricing pricing = new Pricing();
		
		pricing.setBuyerCd(buyerCd);
		pricing.setValidDate(date);
		
		List<Product> list = prs.getProductList(pricing);
		String[] productList = new String[list.size()];
		int i = 0;
		
		for (Product product : list) {
			String code = product.getProductCd() +"(" + product.getPname() + ")";
		
			productList[i] = code;
			i++;
		}
		return productList;
	}
	
	
	// by현서. 엑셀 다운로드
	@RequestMapping("pricingExcelDown")
	@ResponseBody
	public void pricingExcelDown(HttpServletResponse response, @RequestParam(name="pricings")String pricings) throws IOException {
		
		// 출력할 판매가 리스트
		List<Pricing> list = new ArrayList<>();
		Pricing pricingRow = new Pricing();
	
		// jason으로 넘어온 데이터를 변환
		try {
			JSONParser p = new JSONParser();
			Object obj = p.parse(pricings);
			JSONArray arr = JSONArray.fromObject(obj);
			
			Pricing pricing = new Pricing();
			
			for (int i = 0; i < arr.size(); i++) { 
				
				JSONObject itemObj = (JSONObject) arr.get(i);
				String buyerCd = (String) itemObj.get("buyerCd");
				String productCd = (String) itemObj.get("productCd");
				
				String start = (String) itemObj.get("startdate");
				Date sdate = Date.valueOf(start);
				String end = (String) itemObj.get("enddate");
				Date edate = Date.valueOf(end);
				
				pricing.setBuyerCd(buyerCd);
				pricing.setProductCd(productCd);
				pricing.setStartdate(sdate);
				pricing.setEnddate(edate);
				
				// excel출력할 데이터를 불러옴
				pricingRow = prs.listForExcel(pricing);
				list.add(pricingRow);
			}
			
			
		} catch (ParseException e) {
			System.out.println(e.getMessage());
		}
		
		
		// 워크북 생성
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet("판매가 리스트");
		Row row = null;
		Cell cell = null;
		int rowNo = 0;
		
		// 테이블 헤더용 스타일
		CellStyle headStyle = wb.createCellStyle();
		
		// 가는 경계선
		headStyle.setBorderTop(BorderStyle.THIN);
	    headStyle.setBorderBottom(BorderStyle.THIN);
	    headStyle.setBorderLeft(BorderStyle.THIN);
	    headStyle.setBorderRight(BorderStyle.THIN);
	
	    // 배경색 노란색
	    headStyle.setFillForegroundColor(HSSFColorPredefined.YELLOW.getIndex());
	    headStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	    
	    // 데이터 가운데 정렬
	    headStyle.setAlignment(HorizontalAlignment.CENTER);
	    
	    // 데이터용 경계 스타일 테두리만 지정
	    CellStyle bodyStyle = wb.createCellStyle();
	    bodyStyle.setBorderTop(BorderStyle.THIN);
	    bodyStyle.setBorderBottom(BorderStyle.THIN);
	    bodyStyle.setBorderLeft(BorderStyle.THIN);
	    bodyStyle.setBorderRight(BorderStyle.THIN);
	    
	    // 헤더 생성
	    row = sheet.createRow(rowNo++);
	    
	    cell = row.createCell(0);
	    cell.setCellStyle(headStyle);
	    cell.setCellValue("고객코드");
	    
	    cell = row.createCell(1);
	    cell.setCellStyle(headStyle);
	    cell.setCellValue("고객명");
	    
	    cell = row.createCell(2);
	    cell.setCellStyle(headStyle);
	    cell.setCellValue("상품코드");
	    
	    cell = row.createCell(3);
	    cell.setCellStyle(headStyle);
	    cell.setCellValue("상품명");
	    
	    cell = row.createCell(4);
	    cell.setCellStyle(headStyle);
	    cell.setCellValue("판매가");
	    
	    cell = row.createCell(5);
	    cell.setCellStyle(headStyle);
	    cell.setCellValue("계약시작일");
	    
	    cell = row.createCell(6);
	    cell.setCellStyle(headStyle);
	    cell.setCellValue("계약종료일");
	
	    cell = row.createCell(7);
	    cell.setCellStyle(headStyle);
	    cell.setCellValue("할인율");
	    
	    cell = row.createCell(8);
	    cell.setCellStyle(headStyle);
	    cell.setCellValue("최종판매가");
	    
	    cell = row.createCell(9);
	    cell.setCellStyle(headStyle);
	    cell.setCellValue("통화단위");
	    
	    cell = row.createCell(10);
	    cell.setCellStyle(headStyle);
	    cell.setCellValue("등록일");
	    
	    cell = row.createCell(11);
	    cell.setCellStyle(headStyle);
	    cell.setCellValue("상태변경일");
	    
	    
	    
	 // 데이터 부분 생성
	    for(Pricing li : list) {
	
	        row = sheet.createRow(rowNo++);
	
	        cell = row.createCell(0);
	        cell.setCellStyle(bodyStyle);
	        cell.setCellValue(li.getBuyerCd());
	        
	        cell = row.createCell(1);
	        cell.setCellStyle(bodyStyle);
	        cell.setCellValue(li.getBname());
	
	        cell = row.createCell(2);
	        cell.setCellStyle(bodyStyle);
	        cell.setCellValue(li.getProductCd());
	        
	        cell = row.createCell(3); 
	        cell.setCellStyle(bodyStyle);
	        cell.setCellValue(li.getPname());
		    
		    cell = row.createCell(4);
		    cell.setCellStyle(bodyStyle);
		    cell.setCellValue(li.getPrice());
		    
		    cell = row.createCell(5);
		    cell.setCellStyle(bodyStyle);
		    cell.setCellValue(li.getStartdate().toString());
		    
		    cell = row.createCell(6);
		    cell.setCellStyle(bodyStyle);
		    cell.setCellValue(li.getEnddate().toString());
		    
		    cell = row.createCell(7);
		    cell.setCellStyle(bodyStyle);
		    cell.setCellValue(li.getDiscountrate());
		    
		    cell = row.createCell(8);
		    cell.setCellStyle(bodyStyle);
		    cell.setCellValue(li.getPrice() * (1 - (li.getDiscountrate()/100)));
		    
		    cell = row.createCell(9);
		    cell.setCellStyle(bodyStyle);
		    cell.setCellValue(li.getCurrency());
		    
		    cell = row.createCell(10);
		    cell.setCellStyle(bodyStyle);
		    cell.setCellValue(li.getAdddate().toString());
		    
		    cell = row.createCell(11);
		    cell.setCellStyle(bodyStyle);
		    if (li.getStatusdate() != null) {
		    	cell.setCellValue(li.getStatusdate().toString());
			}else {
				cell.setCellValue(li.getStatusdate());
			}
	
	    }
	
	    // 컨텐츠 타입과 파일명 지정
	    response.setContentType("ms-vnd/excel");
	    response.setHeader("Content-Disposition", "attachment;filename=pricing.xlsx");
	    
	    // 엑셀 출력
	    try {
	        wb.write(response.getOutputStream());
	    } finally {
	        wb.close();
	    }
	    
	}
	
}




