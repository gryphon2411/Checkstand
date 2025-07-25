# Checkstand Video Demo Script
**Google Gemma 3n Impact Challenge Submission**
*Target Duration: 3-4 minutes*

---

## **SCENE 1: The Financial Black Hole** *(0:00 - 0:30)*

### **Opening Hook** *(0:00 - 0:15)*
**[Show pile of crumpled receipts on desk next to a smartphone showing a banking app. The banking app shows transactions like "SuperMart $78.50" but no item details.]**

**NARRATOR**: "We try to track our spending, but our bank app only tells us *where* we spent money, not *what* we bought. These receipts... they're a financial black hole. How can you control your budget if you don't know where your money is really going?"

### **The Core Problem** *(0:15 - 0:30)*
**[Quick cuts: coffee shop receipt, grocery store receipt, hardware store receipt.]**

**NARRATOR**: "Our most frequent purchases from supermarkets, cafes, and local shops are often invisible in our digital records. This is where we lose control."

---

## **SCENE 2: The Solution - Private Financial Empowerment** *(0:30 - 1:15)*

### **Meet Checkstand** *(0:30 - 0:45)*
**[Show Checkstand app loading on phone]**

**NARRATOR**: "Meet Checkstand. It's a powerful, simple tool designed to solve this exact problem. It turns your physical receipts into actionable financial data, right on your phone."

### **The Trust Foundation: On-Device AI** *(0:45 - 1:00)*
**[Show animation: A receipt is scanned, and a 'lock' icon stays on the phone. Data doesn't go to a cloud.]**

**NARRATOR**: "But to trust an app with your financial life, you need absolute privacy. That's why we built Checkstand with Google's on-device Gemma 3n model. All the AI processing happens here, on your phone. Nothing is ever uploaded, shared, or seen by anyone else."

### **The "Bigger Than Chatbot" Moment** *(1:00 - 1:15)*
**[Show Google Gemma 3n logo transition to Checkstand interface]**

**NARRATOR**: "The challenge asked us to think bigger than a chatbot. Checkstand answers that call by delivering true financial empowerment, made possible *because* of its private, on-device AI."

### **Development Journey Insight** *(1:15 - 1:30)*
**[Show quick montage: React Native logo → Error screen → Android logo]**

**NARRATOR**: "We actually started building this in React Native, but hit a real constraint - the Metro bundler couldn't handle our 4.4GB AI model. Rather than compromise, we pivoted to native Android, proving our commitment to the best possible solution."

---

## **SCENE 3: Live Demonstration** *(1:30 - 2:45)*

### **Demo Setup** *(1:30 - 1:40)*
**[Show real receipts and phone ready]**

**NARRATOR**: "Let me show you how it works. I have real receipts here, and I'm going to scan them live - no editing, no tricks."

### **First Receipt Scan** *(1:25 - 1:55)*
**[Live camera capture of receipt, show processing]**

**NARRATOR**: "I'll open Checkstand, point the camera at this receipt, and capture. Watch the status indicator - 'Gemma 3n' shows our AI model is ready. Now I'll take the photo..."

**[Show processing indicator: "Processing receipt..."]**

**NARRATOR**: "Behind the scenes, three things happen instantly: First, optical character recognition extracts the text. Then, Google's Gemma 3n model - running entirely on this device - analyzes that text. Finally, smart parsing extracts the merchant, date, and total amount."

### **Results Reveal** *(1:55 - 2:10)*
**[Show parsed receipt data]**

**NARRATOR**: "And there we have it! Target, July 15th, $24.00 - perfectly extracted in about 24 seconds. All processing happened on this device, with zero internet connection required."

### **Second Receipt for Reliability** *(2:10 - 2:30)*
**[Quick second demo]**

**NARRATOR**: "Let me try another one to show consistency... Perfect! Another receipt processed accurately. This isn't a tech demo - it's a production-ready application."

---

## **SCENE 4: Technical Innovation** *(2:30 - 3:00)*

### **Under the Hood** *(2:30 - 2:50)*
**[Show architecture diagram or code snippets]**

**NARRATOR**: "What makes this possible? Checkstand implements a sophisticated multimodal AI pipeline. We use Google's MediaPipe framework to run the 4.4GB Gemma 3n model locally, combined with ML Kit for OCR, all wrapped in a Clean Architecture with robust error handling and smart fallbacks."

### **Privacy Focus** *(2:50 - 3:00)*
**[Show "offline" indicator, airplane mode demo]**

**NARRATOR**: "The privacy implications are profound. Your financial data never leaves your device. It works in airplane mode, in remote areas, in countries with strict data laws - anywhere you need it."

---

## **SCENE 5: Real-World Impact** *(3:00 - 3:30)*

### **Use Cases** *(3:00 - 3:20)*
**[Show different user scenarios: small business, personal finance, accessibility]**

**NARRATOR**: "Who benefits? Small business owners tracking expenses for taxes. Individuals managing personal budgets. People in crisis situations processing insurance claims without internet. Those concerned about financial privacy. Visually impaired users who can leverage voice feedback."

### **The Bigger Picture** *(3:20 - 3:30)*
**[Show global connectivity challenges]**

**NARRATOR**: "In a world where 3 billion people lack reliable internet access, on-device AI isn't just convenient - it's democratizing technology."

---

## **SCENE 6: Call to Action** *(3:30 - 4:00)*

### **Competition Context** *(3:30 - 3:45)*
**[Show Google Gemma 3n Challenge logo]**

**NARRATOR**: "This is our submission for the Google Gemma 3n Impact Challenge, specifically targeting the Google AI Edge Prize for showcasing compelling on-device AI applications."

### **Future Vision** *(3:45 - 4:00)*
**[Show code repository, download links]**

**NARRATOR**: "The complete source code is available under CC BY 4.0 license. Try Checkstand yourself - because your financial privacy shouldn't be the price of convenience. This is AI that works for you, not against you."

**[End with Checkstand logo and "Privacy-First AI"]**

---

## **Production Notes**

### **Visual Requirements**
- Real receipts (variety of stores/formats)
- Clean phone/app interface
- Good lighting for camera capture
- Screen recording capability
- Architecture diagrams or code snippets
- Privacy/security visual metaphors

### **Audio Requirements**
- Clear narration (professional or high-quality home setup)
- Background music (subtle, non-distracting)
- Sound effects for UI interactions (optional)

### **Technical Setup**
- Screen recording software (OBS Studio recommended)
- Video editing software (DaVinci Resolve free option)
- Phone with good camera for receipt capture demonstration
- Stable phone mount for consistent framing

### **Key Messaging**
1. **Privacy-first approach** - data never leaves device
2. **Real utility** - solves genuine problems
3. **Technical excellence** - production-ready implementation
4. **Innovation** - multimodal AI pipeline
5. **Impact** - democratizing financial tools
6. **Competition alignment** - Google AI Edge Prize target

### **Success Metrics**
- Demonstrates working application (not just concept)
- Shows real-time processing without cuts/edits
- Explains technical innovation clearly
- Connects to real-world impact
- Maintains viewer engagement throughout
- Professional presentation quality
